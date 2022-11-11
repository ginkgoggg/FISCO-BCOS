package com.block.manager.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.block.manager.annotation.Audit;
import com.block.manager.constant.Constant;
import com.block.manager.entity.LogFile;
import com.block.manager.entity.vo.FileInfoVO;
import com.block.manager.entity.vo.FileLogInfoVO;
import com.block.manager.entity.vo.FileVO;
import com.block.manager.enums.AuditTypeEnum;
import com.block.manager.enums.ErrorCodeEnum;
import com.block.manager.mapper.LogFileMapper;
import com.block.manager.minio.Minio;
import com.block.manager.service.LogFileService;
import com.block.manager.utils.FileHashUtils;
import com.nstl.common.conversion.ObjectConversion;
import com.nstl.common.exception.BusinessException;
import com.nstl.common.file.FileNameUtils;
import com.nstl.mybatisplus.service.impl.NstlServiceImpl;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * (TLogFile)表服务实现类
 *
 * @author jie.zhong
 * @since 2022-08-31 11:04:00
 */
@Slf4j
@Service
public class LogFileServiceImpl extends NstlServiceImpl<LogFileMapper, LogFile> implements LogFileService {


    @Resource
    private Minio minio;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileVO uploadFile(MultipartFile file, String remoteAddress) throws IOException {
        InputStream inputStream = this.cloneInputStream(file.getInputStream());
        LogFile logFile = new LogFile();
        logFile.setFileName(file.getOriginalFilename());
        String fileSuffix = FileNameUtils.getFileSuffix(file.getOriginalFilename());
        logFile.setFileSuffix(fileSuffix);
        String deskFileName = UUID.randomUUID().toString() + "." + fileSuffix;
        logFile.setFilePath(this.saveFileToDisk(file.getInputStream(), deskFileName));
        logFile.setSrcIp(remoteAddress);
        if(ObjectUtils.isEmpty(inputStream)){
            logFile.setFileSize(0L);
        }else {
            logFile.setFileSize((long) inputStream.available());
        }
        logFile.setHashCode(FileHashUtils.hash(inputStream));
        this.saveOrUpdate(logFile);
        return ObjectConversion.objectConversion(logFile, FileVO.class);
    }

    @Override
    @Audit(template = Constant.READ_MESSAGE_TEMPLATE, targetId = "#fileId", type = AuditTypeEnum.GET)
    public FileInfoVO getFileInfo(Long fileId) throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, InsufficientDataException, ErrorResponseException {
        LogFile logFile = this.getById(fileId);
        if (ObjectUtils.isEmpty(logFile)) {
            throw new BusinessException(ErrorCodeEnum.FILE_NOT_FOUND.getCode(), ErrorCodeEnum.FILE_NOT_FOUND.getMsg());
        }
        FileInfoVO fileInfoVO = new FileInfoVO();
        fileInfoVO.setSrcIp(logFile.getSrcIp());
        fileInfoVO.setId(logFile.getId());
        fileInfoVO.setFileSize(logFile.getFileSize());
        fileInfoVO.setFileName(logFile.getFileName());
        fileInfoVO.setHashcode(logFile.getHashCode());
        fileInfoVO.setFileSize(logFile.getFileSize());
        fileInfoVO.setUploadTime(logFile.getCreateTime());
        fileInfoVO.setFilePath(logFile.getFilePath());
        fileInfoVO.setFullPath(logFile.getFilePath());
        fileInfoVO.setInternetPath(minio.findFile(logFile.getFilePath()));
        return fileInfoVO;
    }

    @Override
    public Page<FileLogInfoVO> listPage(Page<FileLogInfoVO> page,String hashcode, Date startTime, Date endTime) {
        List<FileLogInfoVO> data = this.baseMapper.listPage(page,hashcode,startTime,endTime );
        page.setRecords(data);
        return page;
    }

    private String saveFileToDisk(InputStream inputStream, String fileName) throws IOException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(new Date());
        String fullPath = date + "/" + fileName;
        try {
            minio.putFile(inputStream, fullPath);
        } catch (ServerException | InsufficientDataException | InternalException | InvalidResponseException | InvalidKeyException | NoSuchAlgorithmException | XmlParserException | ErrorResponseException e) {
            throw new BusinessException(ErrorCodeEnum.UPLOAD_FAIL.getCode(), ErrorCodeEnum.UPLOAD_FAIL.getMsg());
        }
        return fullPath;
    }

    private InputStream cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}

