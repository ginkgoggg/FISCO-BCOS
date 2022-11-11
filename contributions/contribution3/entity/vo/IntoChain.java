package com.block.manager.entity.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author jie.zhong
 * @version 1.0
 * @date 2022/9/22 15:48
 */
@Data
public class IntoChain {

    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    @NotNull(message = "上链日志内容不能为空")
    @NotEmpty(message = "上链日志内容不能为空")
    private List<Long> logInfoId;

}
