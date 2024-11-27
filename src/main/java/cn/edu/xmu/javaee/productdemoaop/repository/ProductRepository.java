package cn.edu.xmu.javaee.productdemoaop.repository;

import cn.edu.xmu.javaee.productdemoaop.entity.ProductEntity;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    // 根据商品id查询商品
    @Query(value = "SELECT p.id, p.shop_id, p.goods_id, p.category_id, p.template_id, p.sku_sn, p.name, " +
            "p.original_price, p.weight, p.barcode, p.unit, p.origin_place, p.creator_id, p.creator_name, " +
            "p.modifier_id, p.modifier_name, p.gmt_create, p.gmt_modified, p.status, p.commission_ratio, " +
            "p.shop_logistic_id, p.free_threshold " +
            "FROM goods_product p WHERE p.id = :id", nativeQuery = true)
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<ProductEntity> findByGoodsId(@Param("id") Long id);

    // 根据商品名称查询商品
    // @Query("select p from ProductEntity p where p.name = :name")
    @Query(value = "SELECT p.id, p.shop_id, p.goods_id, p.category_id, p.template_id, p.sku_sn, p.name, " +
            "p.original_price, p.weight, p.barcode, p.unit, p.origin_place, p.creator_id, p.creator_name, " +
            "p.modifier_id, p.modifier_name, p.gmt_create, p.gmt_modified, p.status, p.commission_ratio, " +
            "p.shop_logistic_id, p.free_threshold " +
            "FROM goods_product p WHERE p.name = :name", nativeQuery = true)
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<ProductEntity> findByName(@Param("name") String name);

    // 根据 goodsId 和 id 查询其他商品
    // @Query("select p from ProductEntity p where p.goodsId = :goodsId AND p.id <> :id")
    @Query(value = "SELECT p.id, p.shop_id, p.goods_id, p.category_id, p.template_id, p.sku_sn, p.name, " +
            "p.original_price, p.weight, p.barcode, p.unit, p.origin_place, p.creator_id, p.creator_name, " +
            "p.modifier_id, p.modifier_name, p.gmt_create, p.gmt_modified, p.status, p.commission_ratio, " +
            "p.shop_logistic_id, p.free_threshold " +
            "FROM goods_product p WHERE p.goods_id = :goodsId AND p.id <> :id", nativeQuery = true)
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<ProductEntity> findOtherProducts(@Param("goodsId") Long goodsId, @Param("id") Long id);
}
