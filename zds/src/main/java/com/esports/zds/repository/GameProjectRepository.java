package com.esports.zds.repository;

import com.esports.zds.entity.GameProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 游戏板块仓库接口
 */
@Repository
public interface GameProjectRepository extends JpaRepository<GameProject, Long> {

    /**
     * 根据状态获取游戏板块列表
     */
    List<GameProject> findByStatusOrderBySortOrderAsc(Integer status);

    /**
     * 根据名称查找游戏板块
     */
    GameProject findByName(String name);

    /**
     * 根据显示名称查找游戏板块
     */
    GameProject findByDisplayName(String displayName);
}
