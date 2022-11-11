package com.block.manager.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author jie.zhong
 * @version 1.0
 * @date 2022/9/23 15:10
 */
@Data
public class ChainLogVO {

    private Long fileId;

    private String fileHash;

    private Integer type;

    private Integer logType;

    private String taskId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" , timezone = "GMT+8")
    private Date createTime;

    /**
     * 已上链日志条数
     */
    private Long chainCount;

}
