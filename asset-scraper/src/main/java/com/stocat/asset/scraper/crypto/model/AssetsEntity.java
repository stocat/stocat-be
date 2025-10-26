package com.stocat.asset.scraper.crypto.model;

import com.stocat.asset.scraper.crypto.model.enums.AssetsCategory;
import com.stocat.asset.scraper.crypto.model.enums.Currency;
import com.stocat.common.mysql.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assets")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AssetsEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol; //  마켓 코드

    @Column(nullable = false, length = 100)
    private String koName;

    @Column(nullable = false, length = 100)
    private String usName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetsCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

//    @Column(name = "raw_json", columnDefinition = "longtext", nullable = false)
//    private String rawJson;

    public void deactivate() {
        this.isActive = false;
    }
}
