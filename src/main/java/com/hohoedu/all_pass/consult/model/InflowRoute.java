package com.hohoedu.all_pass.consult.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "inflow_route")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InflowRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer inflowRouteNo;

    @Column(nullable = false, length = 20)
    private String inflowRoute;

    @Builder
    public InflowRoute(Integer inflowRouteNo, String inflowRoute) {
        this.inflowRouteNo = inflowRouteNo;
        this.inflowRoute = inflowRoute;
    }

}
