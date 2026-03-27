package com.sqmusicplus.v3.base.service.impl;


import com.sqmusicplus.v3.base.entity.DownloadInfo;
import com.sqmusicplus.v3.base.mapper.DownloadInfoMapper;
import com.sqmusicplus.v3.base.service.DownloadInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqmusicplus.v3.download.DownloadStatus;
import com.sqmusicplus.v3.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sq
 * @since 2023-08-23
 */
@Service
public class DownloadInfoServiceImpl extends ServiceImpl<DownloadInfoMapper, DownloadInfo> implements DownloadInfoService {


    @Autowired
    private  DownloadInfoServiceImpl downloadInfoService;



    @Override
    public synchronized Boolean add(DownloadInfo downloadInfo) {
        // 截断过长字段，避免数据库 varchar(255) 插入异常
        truncateStringField(downloadInfo);
        downloadInfo.setDownloadStatus(DownloadStatus.waiting.getValue());
        boolean save = downloadInfoService.save(downloadInfo);
        return  save;
    }

    @Override
    public synchronized Boolean add(List<DownloadInfo> downloadInfo) {
        // 使用Set去重，避免重复添加相同歌曲
        Set<String> uniqueMusicIds = new HashSet<>();
        List<DownloadInfo> uniqueDownloadInfo = new ArrayList<>();
        
        for (DownloadInfo info : downloadInfo) {
            // 检查歌曲ID是否已存在
            if (!uniqueMusicIds.contains(info.getDownloadMusicId())) {
                // 截断过长字段，避免数据库 varchar(255) 插入异常
                truncateStringField(info);
                info.setDownloadStatus(DownloadStatus.waiting.getValue());
                uniqueMusicIds.add(info.getDownloadMusicId());
                uniqueDownloadInfo.add(info);
            }
        }
        
        if (uniqueDownloadInfo.isEmpty()) {
            return true;
        }
        
        boolean save = downloadInfoService.saveBatch(uniqueDownloadInfo);
        return save;
    }

    /**
     * 截断 DownloadInfo 中可能超长的字符串字段，防止数据库插入失败。
     */
    private void truncateStringField(DownloadInfo downloadInfo) {
        if (downloadInfo == null) {
            return;
        }
        if (downloadInfo.getDownloadArtistname() != null && downloadInfo.getDownloadArtistname().length() > 255) {
            downloadInfo.setDownloadArtistname(downloadInfo.getDownloadArtistname().substring(0, 255));
        }
        if (downloadInfo.getDownloadMusicname() != null && downloadInfo.getDownloadMusicname().length() > 255) {
            downloadInfo.setDownloadMusicname(downloadInfo.getDownloadMusicname().substring(0, 255));
        }
        if (downloadInfo.getDownloadAlbumname() != null && downloadInfo.getDownloadAlbumname().length() > 255) {
            downloadInfo.setDownloadAlbumname(downloadInfo.getDownloadAlbumname().substring(0, 255));
        }
        if (downloadInfo.getDownloadMsg() != null && downloadInfo.getDownloadMsg().length() > 255) {
            downloadInfo.setDownloadMsg(downloadInfo.getDownloadMsg().substring(0, 255));
        }
    }

    @Override
    public synchronized boolean updateById(DownloadInfo entity) {
        entity.setDownloadUpdateTime(DateUtils.getNowDate());
        return super.updateById(entity);
    }
}
