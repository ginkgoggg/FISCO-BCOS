package com.block.manager.service.impl;

import chain.ChainManager;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.block.manager.config.QueueConfig;
import com.block.manager.current.UsernameHandler;
import com.block.manager.entity.*;
import com.block.manager.entity.vo.*;
import com.block.manager.enums.ErrorCodeEnum;
import com.block.manager.enums.TaskStatusEnum;
import com.block.manager.service.*;
import com.block.manager.template.Redis;
import com.block.manager.utils.Base64Utils;
import com.block.manager.utils.FileHashUtils;
import com.nstl.common.conversion.ObjectConversion;
import com.nstl.common.exception.BusinessException;
import com.nstl.common.factory.ThreadPoolFactory;
import com.nstl.mybatisplus.entity.NstlEntity;
import config.ChainConfig;
import enums.ContractType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.block.manager.enums.ErrorCodeEnum.LOG_NOT_FOUND;

/**
 * @author jie.zhong
 * @version 1.0
 * @date 2022/9/21 17:13
 */
@Slf4j
@Service
public class ChainServiceImpl implements ChainService {

    @Resource
    private LogFileService logFileService;

    @Resource
    private LogService logService;

    @Resource
    private DataObjService dataObjService;

    @Resource
    private QueueConfig queueConfig;

    @Resource
    private Redis redis;

    @Resource
    private MatchedLogService matchedLogService;

    @Resource
    private ChainConfig chainConfig;

    @Resource
    private UsernameHandler usernameHandler;

    @Resource
    private UserService userService;

    @Resource
    private BusinessDataObjService businessDataObjService;

    @Resource
    private BusinessUserService businessUserService;

    @Override
    public String createMatchContent(MatchContentVO matchContentVO) {
        LogFile logFile = logFileService.getById(matchContentVO.getFileId());
        if (ObjectUtils.isEmpty(logFile)) {
            throw new BusinessException(ErrorCodeEnum.FILE_NOT_FOUND.getCode(), ErrorCodeEnum.FILE_NOT_FOUND.getMsg());
        }
        QueryWrapper<Log> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Log::getFileId, matchContentVO.getFileId());
        Log log = logService.getOne(queryWrapper);
        if (ObjectUtils.isEmpty(log)) {
            throw new BusinessException(ErrorCodeEnum.BIND_FAILED.getCode(), ErrorCodeEnum.BIND_FAILED.getMsg());
        }
        QueryWrapper<DataObj> dataObjQueryWrapper = new QueryWrapper<>();
        dataObjQueryWrapper.lambda().in(NstlEntity::getId, matchContentVO.getDataObject());
        List<DataObj> list = dataObjService.list(dataObjQueryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ErrorCodeEnum.DATA_OBJ_NOT_FOUND.getCode(), ErrorCodeEnum.DATA_OBJ_NOT_FOUND.getMsg());
        }
        List<DataObjectMessage.DataObject> dataObjects = new ArrayList<>();
        list.forEach(item -> {
            if (StringUtils.isNotBlank(item.getOperationList())) {
                DataObjectMessage.DataObject dataObject = new DataObjectMessage.DataObject();
                dataObject.setName(item.getName());
                dataObject.setId(item.getId());
                dataObject.setOperationList(Arrays.asList(item.getOperationList().split(",")));
                QueryWrapper<BusinessDataObj> businessDataObjQueryWrapper = new QueryWrapper<>();
                businessDataObjQueryWrapper.lambda().eq(BusinessDataObj::getDataObjId,item.getId());
                List<BusinessDataObj> users = businessDataObjService.list(businessDataObjQueryWrapper);
                if(!CollectionUtils.isEmpty(users)){
                    List<BusinessUser> businessUsers = businessUserService.listByIds(users.stream().map(BusinessDataObj::getBusinessUserId).collect(Collectors.toCollection(ArrayList::new)));
                    dataObject.setKnownUser(businessUsers);
                }
                dataObject.setDesensitizationInfo(Arrays.asList(item.getDesensitizationInfo().split(",")));
                dataObjects.add(dataObject);
            }
        });
        String taskId = UUID.randomUUID().toString();
        DataObjectMessage dataObjectMessage = new DataObjectMessage();
        dataObjectMessage.setFileId(matchContentVO.getFileId());
        dataObjectMessage.setFileHash(logFile.getHashCode());
        dataObjectMessage.setFilePath(logFile.getFilePath());
        dataObjectMessage.setLogType(log.getLogType());
        dataObjectMessage.setStatus(TaskStatusEnum.CREATE.getCode());
        dataObjectMessage.setType(log.getType());
        dataObjectMessage.setDataObj(dataObjects);
        redis.put(queueConfig.getPublicKey() + taskId, JSON.toJSONString(dataObjectMessage));
        redis.publish(queueConfig.getTopic(), taskId);
        return taskId;
    }

    @Override
    public TaskVO query(String taskId) {
        DataObjectMessage dataObjectMessage = redis.get(queueConfig.getPublicKey() + taskId, DataObjectMessage.class);
        // 任务执行失败时 告知前端并删除缓存
        if (dataObjectMessage.getStatus().equals(TaskStatusEnum.FAILED.getCode())) {
            redis.removeKey(queueConfig.getPublicKey() + taskId);
            TaskVO taskVO = ObjectConversion.objectConversion(dataObjectMessage, TaskVO.class);
            taskVO.setTaskId(taskId);
            return taskVO;
        }
        //任务执行成功 查询数据并封装到taskVO的data里
        if (dataObjectMessage.getStatus().equals(TaskStatusEnum.SUCCESS.getCode())) {
            TaskVO taskVO = ObjectConversion.objectConversion(dataObjectMessage, TaskVO.class);
            QueryWrapper<MatchedLog> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(MatchedLog::getTaskId,taskId).orderByDesc(MatchedLog::getCreateTime);
            taskVO.setData(matchedLogService.list(queryWrapper));
            taskVO.setTaskId(taskId);
            return taskVO;
        }
        //任务为 执行中或已创建 则返回响应数据
        TaskVO taskVO = ObjectConversion.objectConversion(dataObjectMessage, TaskVO.class);
        taskVO.setTaskId(taskId);
        return taskVO;
    }

    @Override
    public Page<DataObjectMessage> getTaskList(Long size, Long current) {
        List<DataObjectMessage> data = redis.likePage(queueConfig.getPublicKey(), DataObjectMessage.class, size, current);
        Page<DataObjectMessage> page = new Page<>(current,size);
        page.setRecords(data);
        Set<String> like = redis.like(queueConfig.getPublicKey());
        if(CollectionUtils.isEmpty(like)){
            page.setTotal(0L);
            return page;
        }
        like.removeIf(item -> item.startsWith(queueConfig.getAnalysis()));
        page.setTotal(like.size());
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Object> intoChain(IntoChain intoChain) throws Exception {
        List<Object> onError = new ArrayList<>();
        QueryWrapper<MatchedLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MatchedLog::getTaskId, intoChain.getTaskId()).in(MatchedLog::getId, intoChain.getLogInfoId());
        List<MatchedLog> list = matchedLogService.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(LOG_NOT_FOUND.getCode(), LOG_NOT_FOUND.getMsg());
        }
        LinkedList<String> msgList = list.stream().map(MatchedLog::getMsg).collect(Collectors.toCollection(LinkedList::new));
        if(CollectionUtils.isEmpty(msgList)){
            throw new BusinessException(10007,"日志内容为空");
        }
        String msg=String.join("\n",msgList);
        String msgHash = FileHashUtils.sha1(msg);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<LinkedList<Object>> chainInfo = new LinkedList<>();
        for (MatchedLog matchedLog : list) {
            LinkedList<Object> simpleRow = new LinkedList<>();
            Date now = new Date();
            matchedLog.setLogHash(msgHash);
            simpleRow.add(msgHash);
            simpleRow.add(simpleDateFormat.format(now));
            simpleRow.add(Base64Utils.encryptBASE64(matchedLog.getMsg()));
            simpleRow.add(matchedLog.getDataObject());
            simpleRow.add(matchedLog.getVerbs());
            simpleRow.add(matchedLog.getPreFileHash());
            simpleRow.add(matchedLog.getPreFilePath());
            chainInfo.add(simpleRow);
        }

        List<MatchedLog> failed = new ArrayList<>();
        ChainManager manager = new ChainManager(chainConfig);
        chainInfo.parallelStream().forEach(params ->{
            try {
                Date insertTime = manager.insert(ContractType.TRACEABILITY, params);
                list.get(chainInfo.indexOf(params)).setBlockChainTime(insertTime);
            } catch (Exception e) {
                log.error("上链发生异常:",e);
                // msg
                try {
                    onError.add(Base64Utils.decryptBASE64(params.get(2).toString()));
                } catch (Exception ignored) {
                }
                log.error("日志ID：{} 上链失败", params.get(2));
                failed.add(list.get(chainInfo.indexOf(params)));
            }
        });

        if(!CollectionUtils.isEmpty(failed)){
            list.removeAll(failed);
        }
        if(CollectionUtils.isEmpty(list)){
            return onError;
        }
        matchedLogService.saveOrUpdateBatch(list);
        return onError;
    }

    @Override
    public Page<ChainLogVO> getChainDataPage(Page<ChainLogVO> page,String username, String dataObj, Date start,Date  end,Integer type) {
        Integer currentUserLevel = this.getCurrentUserLevel();
        if(ObjectUtils.isEmpty(currentUserLevel)){
            return page;
        }
        return matchedLogService.getChainDataPage(page,username,currentUserLevel,dataObj,start,end,type);
    }

    @Override
    public Page<MatchedLog>  getDetailByTask(Page<MatchedLog> page,String taskId,String username, String dataObj, Date start,Date end){
        QueryWrapper<MatchedLog> matchedLogQueryWrapper = new QueryWrapper<>();
        matchedLogQueryWrapper.lambda().eq(MatchedLog::getTaskId,taskId);
        List<MatchedLog> list = matchedLogService.list(matchedLogQueryWrapper);
        if(CollectionUtils.isEmpty(list)){
            return page;
        }
        Long objId = list.stream().map(MatchedLog::getDataObjId).findFirst().orElse(null);
        if(ObjectUtils.isEmpty(objId)){
            return page;
        }
        DataObj dbData = dataObjService.getById(objId);
        if(ObjectUtils.isEmpty(dbData)){
            return page;
        }
        if(this.getCurrentUserLevel() == null || dbData.getSecretLevel() < this.getCurrentUserLevel()){
            throw new BusinessException(403,"权限不足");
        }
        QueryWrapper<MatchedLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MatchedLog::getTaskId,taskId).isNotNull(MatchedLog::getBlockChainTime).orderByDesc(MatchedLog::getBlockChainTime)
        .like(StringUtils.isNotBlank(username),MatchedLog::getMsg,username)
        .gt(!ObjectUtils.isEmpty(start),MatchedLog::getTime,start)
        .lt(!ObjectUtils.isEmpty(end),MatchedLog::getTime,end)
        .eq(!StringUtils.isBlank(dataObj),MatchedLog::getDataObject,dataObj);
        return matchedLogService.page(page,queryWrapper);
    }

    private Integer getCurrentUserLevel(){
        String currentUsername = usernameHandler.getCurrentUsername();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(User::getUsername,currentUsername);
        User one = userService.getOne(queryWrapper);
        if(ObjectUtils.isEmpty(one)){
            throw new BusinessException(500,"用户不存在");
        }
        return one.getUserLevel();
    }
}
