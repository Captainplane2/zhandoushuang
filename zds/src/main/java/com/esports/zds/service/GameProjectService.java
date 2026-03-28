package com.esports.zds.service;

import com.esports.zds.entity.GameProject;

import java.util.List;

/**
 * 游戏板块服务接口
 */
public interface GameProjectService {

    /**
     * 获取所有游戏板块
     */
    List<GameProject> listAll();

    /**
     * 获取启用的游戏板块
     */
    List<GameProject> listEnabled();

    /**
     * 根据ID获取游戏板块
     */
    GameProject getById(Long id);

    /**
     * 创建游戏板块
     */
    GameProject create(GameProject gameProject);

    /**
     * 更新游戏板块
     */
    GameProject update(GameProject gameProject);

    /**
     * 删除游戏板块
     */
    void delete(Long id);

    /**
     * 根据名称获取游戏板块
     */
    GameProject getByName(String name);
}
