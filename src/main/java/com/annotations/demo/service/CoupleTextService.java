package com.annotations.demo.service;

import com.annotations.demo.entity.CoupleText;
import org.springframework.data.domain.Page;

public interface CoupleTextService {
    Page<CoupleText> getCoupleTexts(int page, int size);
}
