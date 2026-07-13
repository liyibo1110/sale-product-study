package com.github.liyibo1110.saleproduct.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.liyibo1110.saleproduct.admin.entity.MerchandiseSize;
import com.github.liyibo1110.saleproduct.admin.mapper.MerchandiseSizeMapper;
import com.github.liyibo1110.saleproduct.base.exception.BizException;
import com.github.liyibo1110.saleproduct.base.exception.ErrorEnum;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 尺码基础CRUD
 * @author liyibo
 * @date 2026-07-13 10:30
 */
@Service
public class MerchandiseSizeService {

    private final MerchandiseSizeMapper sizeMapper;

    public MerchandiseSizeService(MerchandiseSizeMapper sizeMapper) {
        this.sizeMapper = sizeMapper;
    }

    public Long create(MerchandiseSize size) {
        MerchandiseSize existing = sizeMapper.selectOne(
                new LambdaQueryWrapper<MerchandiseSize>()
                        .eq(MerchandiseSize::getSizeCode, size.getSizeCode())
                        .last("LIMIT 1"));
        if (existing != null)
            throw BizException.of(ErrorEnum.PARAM_INVALID_VALUE.getCode(), "尺码编码已存在: " + size.getSizeCode());

        sizeMapper.insert(size);
        return size.getId();
    }

    public void update(MerchandiseSize size) {
        sizeMapper.updateById(size);
    }

    public MerchandiseSize getById(Long id) {
        return sizeMapper.selectById(id);
    }

    public List<MerchandiseSize> listByMerchandiseId(Long merchandiseId) {
        return sizeMapper.selectList(
                new LambdaQueryWrapper<MerchandiseSize>()
                        .eq(MerchandiseSize::getMerchandiseId, merchandiseId)
                        .orderByAsc(MerchandiseSize::getSortOrder));
    }

    public void batchCreate(Long merchandiseId, List<MerchandiseSize> sizeList) {
        for (MerchandiseSize size : sizeList) {
            size.setMerchandiseId(merchandiseId);
            create(size);
        }
    }
}
