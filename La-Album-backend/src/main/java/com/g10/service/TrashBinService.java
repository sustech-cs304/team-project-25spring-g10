package com.g10.service;

import com.g10.model.Photo;
import com.g10.model.TrashBin;
import com.g10.repository.PhotoRepository;
import com.g10.repository.TrashBinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrashBinService {
    @Autowired
    private TrashBinRepository trashBinRepository;

    public TrashBinService(PhotoRepository photoRepository) {
    }

    //TODO: find photo
    public Photo findPhotoById(Long photoId) {
        return null;
    }

    //TODO: flip isDeleted of the photo, delete the record in trashBin
    public void restorePhoto(Long photoId) {

    }

    // TODO: check all photos in trashBin, if delete time > 30 days, delete
    public void automaticDeletePhoto() {

    }

    public void setDeleteDate(Long photoId) {}
}