package com.g10.controller;

import com.g10.model.TrashBin;
import com.g10.service.TrashBinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trash")
public class TrashBinController {
    @Autowired
    private TrashBinService trashBinService;

}