package cn.edu.xmu.javaee.productdemoaop.service;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.productdemoaop.dao.ProductDao;
import cn.edu.xmu.javaee.productdemoaop.dao.bo.Product;
import cn.edu.xmu.javaee.productdemoaop.dao.bo.User;
import cn.edu.xmu.javaee.productdemoaop.mapper.RedisUtil;
import cn.edu.xmu.javaee.productdemoaop.mapper.join.ProductJoinMapper;
import cn.edu.xmu.javaee.productdemoaop.mapper.manual.po.ProductAllPo;
import cn.edu.xmu.javaee.productdemoaop.util.CloneFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Service
public class ProductService {

    private Logger logger = LoggerFactory.getLogger(ProductService.class);


    private ProductDao productDao;

    private RedisUtil redisUtil;

    private static final String PRODUCT_CACHE_KEY = "product:";
    private static final long CACHE_TIMEOUT = 3600; // 1小时过期

    @Autowired
    public ProductService(ProductDao productDao, ProductJoinMapper productJoinMapper, RedisUtil redisUtil) {
        this.productDao = productDao;
        this.redisUtil = redisUtil;
    }

    /**
     * 获取某个商品信息，仅展示相关内容
     *
     * @param id 商品id
     * @return 商品对象
     */
    @Transactional(rollbackFor = {BusinessException.class})
    public Product retrieveProductByIDredis(Long id, boolean all) throws BusinessException {
        logger.debug("findProductByIdredis: id = {}, all = {}", id, all);
        String cacheKey = PRODUCT_CACHE_KEY + id;
        // 从缓存获取
        Product product;
        if (this.redisUtil.hasKey(cacheKey))
        {
            product = (Product) redisUtil.get(cacheKey);
            logger.debug("Hit cache: key = {}", cacheKey);
            return product;
        }

        // 缓存未命中
        product = productDao.retrieveProductByIDredis(id, all);

        // 加入缓存
        if (product != null) {
            logger.debug("Store cache: key = {}", cacheKey);
            redisUtil.set(cacheKey, (Serializable) product, CACHE_TIMEOUT);
        }

        return product;
    }

    @Transactional(rollbackFor = {BusinessException.class})
    public Product retrieveProductByID(Long id, boolean all) throws BusinessException {
        logger.debug("findProductById1: id = {}, all = {}", id, all);
        return productDao.retrieveProductByID(id, all);
    }

    /**
     * 用商品名称搜索商品
     *
     * @param name 商品名称
     * @return 商品对象
     */
    @Transactional
    public List<Product> retrieveProductByName(String name, boolean all) throws BusinessException{
        return productDao.retrieveProductByName(name, all);
    }

    /**
     * 新增商品
     * @param product 新商品信息
     * @return 新商品
     */
    @Transactional
    public Product createProduct(Product product, User user) throws BusinessException{
        return productDao.createProduct(product, user);
    }


    /**
     * 修改商品
     * @param product 修改商品信息
     */
    @Transactional
    public void modifyProduct(Product product, User user) throws BusinessException{
        productDao.modiProduct(product, user);
    }

    /** 删除商品
     * @param id 商品id
     * @return 删除是否成功
     */
    @Transactional
    public void deleteProduct(Long id) throws BusinessException {
        productDao.deleteProduct(id);
    }

    @Transactional
    public Product findProductById_manual(Long id) throws BusinessException {
        logger.debug("findProductById_manual: id = {}", id);
        // return productDao.findProductByID_manual(id);

        String cacheKey = PRODUCT_CACHE_KEY + id;


        logger.debug("Cache key generated: {}", cacheKey);
        Product product = (Product) redisUtil.get(cacheKey);
        if (product != null) {
            logger.debug("Hit cache: key = {}", cacheKey);
            return product;
        } else {
            logger.debug("Cache miss: key = {}", cacheKey);
        }


        product = productDao.findProductByID_manual(id);

        if (product != null) {
            logger.debug("Store cache: key = {}", cacheKey);
            redisUtil.set(cacheKey, (Serializable) product, CACHE_TIMEOUT);
        }

        return product;
    }


    /**
     * 使用 Join 根据Id查询商品
     * @param id 商品id
     * @return 商品对象
     */
    @Transactional
    public Product findProdcutById_join(Long id) throws BusinessException {
        logger.debug("findProdcutById_join: id = {}", id);
        // return productDao.findProdcutById_join(id);

        String cacheKey = PRODUCT_CACHE_KEY + id;

        Product product = (Product) redisUtil.get(cacheKey);
        if (product != null) {
            logger.debug("Hit cache: key = {}", cacheKey);
            return product;
        }

        product = productDao.findProdcutById_join(id);

        if (product != null) {
            logger.debug("Store cache: key = {}", cacheKey);
            redisUtil.set(cacheKey, (Serializable) product, CACHE_TIMEOUT);
        }

        return product;
    }


    /**
     * 用商品名称搜索商品
     *
     * @param name 商品名称
     * @return 商品对象
     */
    @Transactional
    public List<Product> findProductByName_manual(String name) throws BusinessException{
        return productDao.findProductByName_manual(name);
    }

    /**
     * 使用 Join 根据名称查询商品
     * @param name 商品名称
     * @return 商品对象
     */
    @Transactional
    public List<Product> findProductByName_join(String name) throws BusinessException{
        logger.debug("findProductByName_join: name = {}", name);
        return productDao.findProdcutByName_join(name);
    }

    /**
     * 使用 spring data jpa 方法根据名称查询商品
     * @param name 商品名称
     * @return 商品对象
     */
    @Transactional
    public List<Product> findProductByName_jpa(String name, boolean all) throws BusinessException{
        logger.debug("findProductByName_jpa: name = {}", name);
        return productDao.findProductByName_jpa(name, all);
    }

    /**
     * 使用 spring data jpa 方法根据名称查询商品
     * @param productId 商品id
     * @return 商品对象
     */
    @Transactional
    public Product findProductById_jpa(Long productId) throws BusinessException{
        logger.debug("findProductById_jpa: productId = {}", productId);
        // return productDao.findProductByID_jpa(productId);

        String cacheKey = PRODUCT_CACHE_KEY + productId;

        Product product = (Product) redisUtil.get(cacheKey);
        if (product != null) {
            logger.debug("Hit cache: key = {}", cacheKey);
            return product;
        }

        product = productDao.findProductByID_jpa(productId);

        if (product != null) {
            logger.debug("Store cache: key = {}", cacheKey);
            redisUtil.set(cacheKey, (Serializable) product, CACHE_TIMEOUT);
        }

        return product;
    }

}
