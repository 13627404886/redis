//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.javaee.productdemoaop.dao;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.productdemoaop.dao.bo.OnSale;
import cn.edu.xmu.javaee.productdemoaop.dao.bo.Product;
import cn.edu.xmu.javaee.productdemoaop.dao.bo.User;
import cn.edu.xmu.javaee.productdemoaop.entity.OnSaleEntity;
import cn.edu.xmu.javaee.productdemoaop.entity.ProductEntity;
import cn.edu.xmu.javaee.productdemoaop.mapper.generator.ProductPoMapper;
import cn.edu.xmu.javaee.productdemoaop.mapper.generator.po.ProductPo;
import cn.edu.xmu.javaee.productdemoaop.mapper.generator.po.ProductPoExample;
import cn.edu.xmu.javaee.productdemoaop.mapper.join.ProductJoinMapper;
import cn.edu.xmu.javaee.productdemoaop.mapper.join.po.ProductJoinPo;
import cn.edu.xmu.javaee.productdemoaop.mapper.manual.ProductAllMapper;
import cn.edu.xmu.javaee.productdemoaop.mapper.manual.po.ProductAllPo;
import cn.edu.xmu.javaee.productdemoaop.repository.OnSaleRepository;
import cn.edu.xmu.javaee.productdemoaop.repository.ProductRepository;
import cn.edu.xmu.javaee.productdemoaop.util.CloneFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ming Qiu
 **/
@Repository
public class ProductDao {

    private final static Logger logger = LoggerFactory.getLogger(ProductDao.class);
    private final ProductJoinMapper productJoinMapper;

    private ProductPoMapper productPoMapper;

    private OnSaleDao onSaleDao;

    private ProductAllMapper productAllMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OnSaleRepository onSaleRepository;

    @Autowired
    public ProductDao(ProductPoMapper productPoMapper, OnSaleDao onSaleDao, ProductAllMapper productAllMapper,
                      ProductJoinMapper productJoinMapper) {
        this.productPoMapper = productPoMapper;
        this.onSaleDao = onSaleDao;
        this.productAllMapper = productAllMapper;
        this.productJoinMapper = productJoinMapper;
    }

    /**
     * 用GoodsPo对象找Goods对象
     * @param name
     * @return  Goods对象列表，带关联的Product返回
     */
    public List<Product> retrieveProductByName(String name, boolean all) throws BusinessException {
        List<Product> productList = new ArrayList<>();
        ProductPoExample example = new ProductPoExample();
        ProductPoExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(name);
        List<ProductPo> productPoList = productPoMapper.selectByExample(example);
        for (ProductPo po : productPoList){
            Product product = null;
            if (all) {
                product = this.retrieveFullProduct(po);
            } else {
                product = CloneFactory.copy(new Product(), po);
            }
            productList.add(product);
        }
        logger.debug("retrieveProductByName: productList = {}", productList);
        return productList;
    }

    /**
     * 用GoodsPo对象找Goods对象
     * @param  productId
     * @return  Goods对象列表，带关联的Product返回
     */

    public Product retrieveProductByIDredis(Long productId, boolean all) throws BusinessException {
        Product product = null;
        ProductPo productPo = productPoMapper.selectByPrimaryKey(productId);
        if (null == productPo){
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "产品id不存在");
        }
        if (all) {
            product = this.retrieveFullProductredis(productPo);
        } else {
            product = CloneFactory.copy(new Product(), productPo);
        }

        logger.debug("retrieveProductByID: product = {}",  product);
        return product;
    }

    public Product retrieveProductByID(Long productId, boolean all) throws BusinessException {
        Product product = null;
        ProductPo productPo = productPoMapper.selectByPrimaryKey(productId);
        if (null == productPo){
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "产品id不存在");
        }
        if (all) {
            product = this.retrieveFullProduct(productPo);
        } else {
            product = CloneFactory.copy(new Product(), productPo);
        }

        logger.debug("retrieveProductByID: product = {}",  product);
        return product;
    }


    private Product retrieveFullProductredis(ProductPo productPo) throws DataAccessException{
        assert productPo != null;
        Product product =  CloneFactory.copy(new Product(), productPo);
        List<OnSale> latestOnSale = onSaleDao.getLatestOnSaleredis(productPo.getId());
        product.setOnSaleList(latestOnSale);

        List<Product> otherProduct = this.retrieveOtherProduct(productPo);
        product.setOtherProduct(otherProduct);

        return product;
    }

    private Product retrieveFullProduct(ProductPo productPo) throws DataAccessException{
        assert productPo != null;
        Product product =  CloneFactory.copy(new Product(), productPo);
        List<OnSale> latestOnSale = onSaleDao.getLatestOnSale(productPo.getId());
        product.setOnSaleList(latestOnSale);

        List<Product> otherProduct = this.retrieveOtherProduct(productPo);
        product.setOtherProduct(otherProduct);

        return product;
    }


    private List<Product> retrieveOtherProduct(ProductPo productPo) throws DataAccessException{
        assert productPo != null;

        ProductPoExample example = new ProductPoExample();
        ProductPoExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(productPo.getGoodsId());
        criteria.andIdNotEqualTo(productPo.getId());
        List<ProductPo> productPoList = productPoMapper.selectByExample(example);
        return productPoList.stream().map(po->CloneFactory.copy(new Product(), po)).collect(Collectors.toList());
    }

    /**
     * 创建Goods对象
     * @param product 传入的Goods对象
     * @return 返回对象ReturnObj
     */
    public Product createProduct(Product product, User user) throws BusinessException{

        Product retObj = null;
        product.setCreator(user);
        product.setGmtCreate(LocalDateTime.now());
        ProductPo po = CloneFactory.copy(new ProductPo(), product);
        int ret = productPoMapper.insertSelective(po);
        retObj = CloneFactory.copy(new Product(), po);
        return retObj;
    }

    /**
     * 修改商品信息
     * @param product 传入的product对象
     * @return void
     */
    public void modiProduct(Product product, User user) throws BusinessException{
        product.setGmtModified(LocalDateTime.now());
        product.setModifier(user);
        ProductPo po = CloneFactory.copy(new ProductPo(), product);
        int ret = productPoMapper.updateByPrimaryKeySelective(po);
        if (ret == 0 ){
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
    }

    /**
     * 删除商品，连带规格
     * @param id 商品id
     * @return
     */
    public void deleteProduct(Long id) throws BusinessException{
        int ret = productPoMapper.deleteByPrimaryKey(id);
        if (ret == 0) {
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
    }

    public List<Product> findProductByName_manual(String name) throws BusinessException {
        List<Product> productList;
        ProductPoExample example = new ProductPoExample();
        ProductPoExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(name);
        List<ProductAllPo> productPoList = productAllMapper.getProductWithAll(example);
        productList =  productPoList.stream().map(o->CloneFactory.copy(new Product(), o)).collect(Collectors.toList());
        logger.debug("findProductByName_manual: productList = {}", productList);
        return productList;
    }

    /**
     * 用GoodsPo对象找Goods对象
     * @param  productId
     * @return  Goods对象列表，带关联的Product返回
     */
    public Product findProductByID_manual(Long productId) throws BusinessException {
        Product product = null;
        ProductPoExample example = new ProductPoExample();
        ProductPoExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(productId);
        List<ProductAllPo> productPoList = productAllMapper.getProductWithAll(example);

        if (productPoList.isEmpty()){
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "产品id不存在");
        }
        product = CloneFactory.copy(new Product(), productPoList.get(0));
        logger.debug("findProductByID_manual: product = {}", product);
        return product;
    }



    /**
     * Join查询后，将ProductJoinPo对象加工为ProductAllPo对象
     * @param productId
     * @return Goods对象列表，带关联的Product返回
     */
    public Product findProdcutById_join(Long productId) throws BusinessException {
        Product product = null;

        List<ProductJoinPo> productJoinPoList = productJoinMapper.getProductByIdWithJoin(productId);

        if (productJoinPoList.isEmpty()) {
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "产品不存在");
        }

        Map<String, ProductAllPo> productPoMap = new HashMap<>();

        for (ProductJoinPo joinPo : productJoinPoList) {
            String name = joinPo.getName();

            ProductAllPo productAllPo = productPoMap.get(name);
            if (productAllPo == null) {
                productAllPo = ProductAllPo.builder()
                        .id(joinPo.getId())
                        .skuSn(joinPo.getSkuSn())
                        .name(name)
                        .originalPrice(joinPo.getOriginalPrice())
                        .weight(joinPo.getWeight())
                        .barcode(joinPo.getBarcode())
                        .unit(joinPo.getUnit())
                        .originPlace(joinPo.getOriginPlace())
                        .creatorId(joinPo.getCreatorId())
                        .creatorName(joinPo.getCreatorName())
                        .modifierId(joinPo.getModifierId())
                        .modifierName(joinPo.getModifierName())
                        .gmtCreate(joinPo.getGmtCreate())
                        .gmtModified(joinPo.getGmtModified())
                        .commissionRatio(joinPo.getCommissionRatio())
                        .freeThreshold(joinPo.getFreeThreshold())
                        .status(joinPo.getStatus())
                        .otherProduct(new ArrayList<>())
                        .onSaleList(new ArrayList<>())
                        .build();

                productPoMap.put(name, productAllPo);
            }

            productAllPo.getOtherProduct().add(joinPo.getOtherProduct());
            productAllPo.getOnSaleList().add(joinPo.getOnSaleList());
        }

        List<ProductAllPo> productPoList = new ArrayList<>(productPoMap.values());

        product = CloneFactory.copy(new Product(), productPoList.get(0));
        logger.debug("findProductById_Join: product = {}", product);
        return product;
    }


    /**
     * Join查询后，将ProductJoinPo对象加工为ProductAllPo对象
     * @param name
     * @return Goods对象列表，带关联的Product返回
     */
    public List<Product> findProdcutByName_join(String name) throws BusinessException {
        List<Product> productList;

        List<ProductJoinPo> productJoinPoList = productJoinMapper.getProductByNameWithJoin(name);

        if (productJoinPoList.isEmpty()) {
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "产品不存在");
        }

        ProductJoinPo joinPo = productJoinPoList.get(0);

        ProductAllPo productAllPo = ProductAllPo.builder()
                .id(joinPo.getId())
                .skuSn(joinPo.getSkuSn())
                .name(name)
                .originalPrice(joinPo.getOriginalPrice())
                .weight(joinPo.getWeight())
                .barcode(joinPo.getBarcode())
                .unit(joinPo.getUnit())
                .originPlace(joinPo.getOriginPlace())
                .creatorId(joinPo.getCreatorId())
                .creatorName(joinPo.getCreatorName())
                .modifierId(joinPo.getModifierId())
                .modifierName(joinPo.getModifierName())
                .gmtCreate(joinPo.getGmtCreate())
                .gmtModified(joinPo.getGmtModified())
                .commissionRatio(joinPo.getCommissionRatio())
                .freeThreshold(joinPo.getFreeThreshold())
                .status(joinPo.getStatus())
                .otherProduct(new ArrayList<>())
                .onSaleList(new ArrayList<>())
                .build();

        for (ProductJoinPo po : productJoinPoList) {
            productAllPo.getOtherProduct().add(po.getOtherProduct());
            productAllPo.getOnSaleList().add(po.getOnSaleList());
        }

        List<ProductAllPo> productPoList = new ArrayList<>();
        productPoList.add(productAllPo);

        productList =  productPoList.stream().map(o->CloneFactory.copy(new Product(), o)).collect(Collectors.toList());
        logger.debug("findProductByName_join: productList = {}", productList);
        return productList;
    }

    /**
     * 使用 spring data jpa 方法根据名称查询商品
     * @param name 商品名称
     * @return 商品对象
     */
    public List<Product> findProductByName_jpa(String name, boolean all) throws BusinessException {
        List<ProductEntity> productEntityList = productRepository.findByName(name);

        List<Product> productList = new ArrayList<>();
        for (ProductEntity entity : productEntityList) {
            Product product = null;
            if (all) {
                product = findFullProduct_jpa(entity);
            } else {
                product = CloneFactory.copy(new Product(), entity);
            }
            productList.add(product);
        }

        logger.debug("findProductByName_jpa: productList = {}", productList);
        return productList;
    }

    private Product findFullProduct_jpa(ProductEntity productEntity) throws DataAccessException {
        Product product = CloneFactory.copy(new Product(), productEntity);

        LocalDateTime now = LocalDateTime.now();
        List<OnSaleEntity> latestOnSaleEntities = onSaleRepository.findLatestOnSale(productEntity.getId(), now);
        List<OnSale> latestOnSale = latestOnSaleEntities.stream()
                .map(entity -> CloneFactory.copy(new OnSale(), entity))
                .collect(Collectors.toList());
        product.setOnSaleList(latestOnSale);

        List<Product> otherProducts = findOtherProduct_jpa(productEntity);
        product.setOtherProduct(otherProducts);

        return product;
    }

    private List<Product> findOtherProduct_jpa(ProductEntity productEntity) throws DataAccessException {
        List<ProductEntity> otherProductEntities = productRepository.findOtherProducts(
                productEntity.getGoodsId(), productEntity.getId());

        List<Product> otherProducts = new ArrayList<>();
        for (ProductEntity otherEntity : otherProductEntities) {
            otherProducts.add(CloneFactory.copy(new Product(), otherEntity));
        }

        return otherProducts;
    }

    /**
     * 使用 spring data jpa 方法根据名称查询商品
     * @param productId 商品 id
     * @return 商品对象
     */
    public Product findProductByID_jpa(Long productId) throws BusinessException {
        Product product = null;

        List<ProductEntity> productEntityList = productRepository.findByGoodsId(productId);

        if (productEntityList.isEmpty()) {
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "产品id不存在");
        }

        product = findFullProduct_jpa(productEntityList.get(0));

        logger.debug("findProductByID_jpa: product = {}", product);

        return product;
    }
}
