package cn.edu.xmu.javaee.productdemoaop.mapper.join.po;

import cn.edu.xmu.javaee.productdemoaop.mapper.generator.po.OnSalePo;
import cn.edu.xmu.javaee.productdemoaop.mapper.generator.po.ProductPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品规格
 * @author Yiwei Wu
 * CreateTime： 10/25/2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductJoinPo {
    /**
     * 代理对象
     */
    private Long id;

    private ProductPo otherProduct;

    private OnSalePo onSaleList;

    private String skuSn;

    private String name;

    private Long originalPrice;

    private Long weight;

    private String barcode;

    private String unit;

    private String originPlace;

    private Long creatorId;

    private String creatorName;

    private Long modifierId;

    private String modifierName;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

    private Integer commissionRatio;

    private Long freeThreshold;

    private Byte status;

}