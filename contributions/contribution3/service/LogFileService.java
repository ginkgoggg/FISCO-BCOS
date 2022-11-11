package com.block.manager.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.block.manager.entity.LogFile;
import com.block.manager.entity.vo.FileInfoVO;
import com.block.manager.entity.vo.FileLogInfoVO;
import com.block.manager.entity.vo.FileVO;
import com.nstl.mybatisplus.service.NstlService;
import io.minio.errors.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * (TLogFile)表服务接口
 *
 * @author jie.zhong
 * @since 2022-08-31 11:04:00
 */
public interface LogFileService extends NstlService<LogFile> {

    /**
     * 上传文件
     *
     * @param file          文件流
     * @param remoteAddress 远端地址
     * @return 上传结果
     * @throws IOException IO异常
     */
    FileVO uploadFile(MultipartFile file, String remoteAddress) throws IOException;

    /**
     * 获取文件信息
     *
     * @param fileId 文件ID
     * @return 文件信息
     */
    FileInfoVO getFileInfo(Long fileId) throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, InsufficientDataException, ErrorResponseException;

    /**
     * 分页查询数据
     * @param page 页码
     * @param endTime  结束时间
     * @param hashcode  数字摘要
     * @param startTime  开始时间
     * @return 查询结果
     */
    Page<FileLogInfoVO> listPage(Page<FileLogInfoVO> page, String hashcode, Date startTime, Date endTime);
}

