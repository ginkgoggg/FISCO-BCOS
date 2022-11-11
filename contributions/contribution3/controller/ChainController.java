package com.block.manager.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.block.manager.entity.MatchedLog;
import com.block.manager.entity.vo.ChainLogVO;
import com.block.manager.entity.vo.IntoChain;
import com.block.manager.entity.vo.MatchContentVO;
import com.block.manager.service.ChainService;
import com.nstl.common.response.BaseControllerInterface;
import com.nstl.common.response.ResponseBean;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author jie.zhong
 * @version 1.0
 * @date 2022/9/21 17:08
 */
@Validated
@RestController
@RequestMapping("chain")
public class ChainController implements BaseControllerInterface {

    @Resource
    private ChainService chainService;

    @PostMapping("createMatchContent")
    public ResponseBean matchContent(@RequestBody @Valid MatchContentVO matchContentVO) {
        return success(chainService.createMatchContent(matchContentVO));
    }


    @GetMapping("getTaskById")
    public ResponseBean query(@NotBlank(message = "任务ID不能为空") String taskId) {
        return success(chainService.query(taskId));
    }


    @GetMapping("taskList")
    public ResponseBean taskList(@RequestParam(defaultValue = "10") @Min(value = 1, message = "每页至少展示1条") Long size, @RequestParam(defaultValue = "1") @Min(value = 1, message = "最小页码为1") Long current) {
        return success(chainService.getTaskList(size, current));
    }


    @PostMapping("intoChain")
    public ResponseBean intoChain(@RequestBody @Valid IntoChain intoChain) throws Exception {
        List<Object> objects = chainService.intoChain(intoChain);
        return success(Collections.singletonMap("onError", objects));
    }


    @GetMapping("getChainDataPage")
    public  ResponseBean getChainDataPage(Page<ChainLogVO> page, String username, String dataObj,
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date start,
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")Date end,Integer type){
        return  success(chainService.getChainDataPage(page,username,dataObj,start,end,type));
    }

    @GetMapping("getDetailByTask")
    public ResponseBean getDetailByTask(Page<MatchedLog> page , @NotBlank(message = "任务ID不能为空") String taskId,
                                        String username, String dataObj,
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date start,
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")Date end){
        return success(chainService.getDetailByTask(page,taskId,username,dataObj,start,end));
    }

}
