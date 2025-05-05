package com.annotations.demo.service;

import com.annotations.demo.entity.CoupleText;
import com.annotations.demo.repository.CoupleTextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class CoupleTextServiceImpl implements CoupleTextService {
    @Autowired
    private CoupleTextRepository coupleTextRepository;

    @Override
    public Page<CoupleText> getCoupleTexts(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return coupleTextRepository.findAll(pageable);
    }
}
