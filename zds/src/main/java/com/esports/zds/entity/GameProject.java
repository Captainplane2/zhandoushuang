package com.esports.zds.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 游戏板块实体类
 */
@Data
@Entity
@Table(name = "game_project")
public class GameProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // 游戏名称 (如：CS2, LOL, WZRY)

    @Column(nullable = false)
    private String displayName; // 显示名称 (如：CS2, 英雄联盟, 王者荣耀)

    private String icon; // 游戏图标

    private Integer sortOrder; // 排序顺序

    private Integer status; // 状态：0 启用, 1 禁用
}
