package com.stocat.common.domain.asset.domain;

import com.stocat.common.domain.BaseEntity;
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
}
