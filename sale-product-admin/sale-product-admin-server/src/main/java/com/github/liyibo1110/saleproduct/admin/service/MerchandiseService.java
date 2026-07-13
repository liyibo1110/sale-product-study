package com.github.liyibo1110.saleproduct.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.liyibo1110.saleproduct.admin.entity.Merchandise;
import com.github.liyibo1110.saleproduct.admin.mapper.MerchandiseMapper;
import com.github.liyibo1110.saleproduct.base.exception.BizException;
import com.github.liyibo1110.saleproduct.base.exception.ErrorEnum;
import org.springframework.stereotype.Service;

/**
 * 商品基础CRUD
 * @author liyibo
 * @date 2026-07-13 10:22
 */
@Service
public class MerchandiseService {

    private final MerchandiseMapper merchandiseMapper;

    public MerchandiseService(MerchandiseMapper merchandiseMapper) {
        this.merchandiseMapper = merchandiseMapper;
    }

    public Long create(Merchandise merchandise) {
        // code唯一性检查
        Merchandise existing = merchandiseMapper.selectOne(
                new LambdaQueryWrapper<Merchandise>()
                        .eq(Merchandise::getMerchandiseCode, merchandise.getMerchandiseCode())
                        .last("LIMIT 1"));
        if (existing != null)
            throw BizException.of(ErrorEnum.PARAM_INVALID_VALUE.getCode(), "商品编码已存在: " + merchandise.getMerchandiseCode());
        merchandiseMapper.insert(merchandise);
        return merchandise.getId();
    }

    public void update(Merchandise merchandise) {
        merchandiseMapper.updateById(merchandise);
    }

    public Merchandise getById(Long id) {
        return merchandiseMapper.selectById(id);
    }

    public Merchandise getByCode(String merchandiseCode) {
        return merchandiseMapper.selectOne(
                new LambdaQueryWrapper<Merchandise>()
                        .eq(Merchandise::getMerchandiseCode, merchandiseCode)
                        .last("LIMIT 1"));
    }
}
