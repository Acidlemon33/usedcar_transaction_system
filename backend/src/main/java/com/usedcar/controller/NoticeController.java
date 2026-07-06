package com.usedcar.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.usedcar.po.NoticePO;
import com.usedcar.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notice")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticePO> getById(@PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.getById(noticeId));
    }

    @GetMapping("/page")
    public ResponseEntity<IPage<NoticePO>> paginQuery(
            NoticePO notice,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        return ResponseEntity.ok(noticeService.paginQuery(notice, pageNum, pageSize));
    }

    @PostMapping
    public ResponseEntity<Boolean> save(@RequestBody NoticePO notice) {
        return ResponseEntity.ok(noticeService.save(notice));
    }

    @PutMapping
    public ResponseEntity<Boolean> update(@RequestBody NoticePO notice) {
        return ResponseEntity.ok(noticeService.updateById(notice));
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Boolean> remove(@PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.removeById(noticeId));
    }
}