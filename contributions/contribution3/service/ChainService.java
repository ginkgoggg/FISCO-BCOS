package com.block.manager.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.block.manager.entity.MatchedLog;
import com.block.manager.entity.vo.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

/**
 * @author jie.zhong
 * @version 1.0
 * @date 2022/9/21 17:12
 */
public interface ChainService {

    /**
     * 创建匹配请求
     * @param matchContentVO 匹配对象
     * @return 任务ID
     */
    String createMatchContent(MatchContentVO matchContentVO);

    /**
     * 根据任务ID查询任务
     * @param taskId 任务ID
     * @return 查询结果
     */
    TaskVO query(String taskId);

    /**
     * 获取任务列表
     * @param current 页码
     * @param size 每页条数
     * @return 任务列表
     */
    Page<DataObjectMessage> getTaskList(Long size,Long current);

    /**
     * 日志内容上链
     * @param intoChain 上链
     * @return 上链失败的
     */
    List<Object> intoChain(IntoChain intoChain) throws Exception;

    /**
     * 分页查询已上链数据
     * @param dataObj  数据对象
     * @param end  结束时间
     * @param start  开始时间
     * @param type  类型
     * @param username  用户名称
     * @param page 分页数据
     * @return 查询结果
     */
    Page<ChainLogVO> getChainDataPage(Page<ChainLogVO> page, String username, String dataObj, Date start,Date  end,Integer type);

    /**
     * 查询上链详情
     * @param taskId 任务ID
     * @param username 用户名
     * @param start 开始时间
     * @param end 结束时间
     * @param dataObj 数据对象
     * @param page 分页
     * @return 查询结果
     */
    Page<MatchedLog>  getDetailByTask(Page<MatchedLog> page,String taskId,String username, String dataObj, Date start,Date end);
}
