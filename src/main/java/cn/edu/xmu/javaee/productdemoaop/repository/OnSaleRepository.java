package cn.edu.xmu.javaee.productdemoaop.repository;

import cn.edu.xmu.javaee.productdemoaop.entity.OnSaleEntity;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OnSaleRepository extends JpaRepository<OnSaleEntity, Long> {
    // @Query("SELECT o FROM OnSaleEntity o WHERE o.productId = :productId " +
    //         "AND o.beginTime <= :now AND o.endTime >= :now " +
    //         "ORDER BY o.endTime DESC")
    @Query(value = "SELECT o.id, o.shop_id, o.product_id, o.price, o.begin_time, o.end_time, " +
            "o.quantity, o.type, o.creator_id, o.creator_name, o.modifier_id, o.modifier_name, " +
            "o.gmt_create, o.gmt_modified, o.max_quantity, o.invalid " +
            "FROM goods_onsale o WHERE o.product_id = :productId " +
            "AND o.begin_time <= :now AND o.end_time >= :now " +
            "ORDER BY o.end_time DESC", nativeQuery = true)
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<OnSaleEntity> findLatestOnSale(@Param("productId") Long productId, @Param("now") LocalDateTime now);
}
