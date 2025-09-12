package com.hohoedu.all_pass.consult.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_inflow_route", uniqueConstraints = @UniqueConstraint(name = "uq_inflow_route_key", columnNames = "inflow_route_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InflowRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "inflow_route_key", nullable = false, length = 20)
    private String inflowRouteKey;

    @Column(name = "inflow_route_name", nullable = false, length = 20)
    private String inflowRouteName;

    @Builder
    public InflowRoute(String inflowRouteKey, String inflowRouteName) {
        this.inflowRouteKey = inflowRouteKey;
        this.inflowRouteName = inflowRouteName;
    }
}
