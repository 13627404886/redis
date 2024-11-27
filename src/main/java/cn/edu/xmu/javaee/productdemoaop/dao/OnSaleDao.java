//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.javaee.productdemoaop.dao;

import cn.edu.xmu.javaee.productdemoaop.dao.bo.OnSale;
import cn.edu.xmu.javaee.productdemoaop.mapper.RedisUtil;
import cn.edu.xmu.javaee.productdemoaop.mapper.generator.OnSalePoMapper;
import cn.edu.xmu.javaee.productdemoaop.mapper.generator.po.OnSalePo;
import cn.edu.xmu.javaee.productdemoaop.mapper.generator.po.OnSalePoExample;
import cn.edu.xmu.javaee.productdemoaop.util.CloneFactory;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OnSaleDao {

    private final static Logger logger = LoggerFactory.getLogger(OnSaleDao.class);

    private OnSalePoMapper onSalePoMapper;

    private RedisUtil redisUtil;

    @Autowired
    public OnSaleDao(OnSalePoMapper onSalePoMapper, RedisUtil redisUtil) {
        this.onSalePoMapper = onSalePoMapper;
        this.redisUtil = redisUtil;
    }

    private static final String ONSALE_CACHE_KEY = "onsale-product:";
    private static final long CACHE_TIMEOUT = 3600;  // 1个小时


    /**
     * 获得货品的最近的价格和库存
     * @param productId 货品对象
     * @return 规格对象
     */
    public List<OnSale> getLatestOnSaleredis(Long productId) throws DataAccessException {
        // LocalDateTime now = LocalDateTime.now();
        // OnSalePoExample example = new OnSalePoExample();
        // example.setOrderByClause("end_time DESC");
        // OnSalePoExample.Criteria criteria = example.createCriteria();
        // criteria.andProductIdEqualTo(productId);
        // criteria.andBeginTimeLessThanOrEqualTo(now);
        // criteria.andEndTimeGreaterThanOrEqualTo(now);
        // List<OnSalePo> onsalePoList = onSalePoMapper.selectByExample(example);
        // return onsalePoList.stream().map(po-> CloneFactory.copy(new OnSale(), po)).collect(Collectors.toList());


        String cacheKey = ONSALE_CACHE_KEY + productId;
        List<OnSale> onSaleList ;
        if (this.redisUtil.hasKey(cacheKey))
        {
            onSaleList = (List<OnSale>) redisUtil.get(cacheKey);
            logger.debug("Hit cache: key = {}", cacheKey);
            return onSaleList;
        }

        OnSalePoExample example = new OnSalePoExample();
        OnSalePoExample.Criteria criteria = example.createCriteria();
        criteria.andProductIdEqualTo(productId);
        criteria.andBeginTimeLessThanOrEqualTo(LocalDateTime.now());
        criteria.andEndTimeGreaterThanOrEqualTo(LocalDateTime.now());
        example.setOrderByClause("end_time DESC");

        List<OnSalePo> onSalePoList = onSalePoMapper.selectByExample(example);
        onSaleList = onSalePoList.stream()
                .map(po -> CloneFactory.copy(new OnSale(), po))
                .collect(Collectors.toList());


        logger.debug("Store cache: key = {}", cacheKey);
        redisUtil.set(cacheKey, (Serializable) onSaleList, CACHE_TIMEOUT);

        return onSaleList;
    }

    public List<OnSale> getLatestOnSale(Long productId) throws DataAccessException {
        LocalDateTime now = LocalDateTime.now();
        OnSalePoExample example = new OnSalePoExample();
        example.setOrderByClause("end_time DESC");
        OnSalePoExample.Criteria criteria = example.createCriteria();
        criteria.andProductIdEqualTo(productId);
        criteria.andBeginTimeLessThanOrEqualTo(now);
        criteria.andEndTimeGreaterThanOrEqualTo(now);
        List<OnSalePo> onsalePoList = onSalePoMapper.selectByExample(example);
        return onsalePoList.stream().map(po-> CloneFactory.copy(new OnSale(), po)).collect(Collectors.toList());
    }
}
