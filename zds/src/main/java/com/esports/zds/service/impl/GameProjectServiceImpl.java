package com.esports.zds.service.impl;

import com.esports.zds.entity.GameProject;
import com.esports.zds.repository.GameProjectRepository;
import com.esports.zds.service.GameProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 游戏板块服务实现类
 */
@Service
public class GameProjectServiceImpl implements GameProjectService {

    @Autowired
    private GameProjectRepository gameProjectRepository;

    @Override
    public List<GameProject> listAll() {
        return gameProjectRepository.findAll();
    }

    @Override
    public List<GameProject> listEnabled() {
        return gameProjectRepository.findByStatusOrderBySortOrderAsc(0);
    }

    @Override
    public GameProject getById(Long id) {
        return gameProjectRepository.findById(id).orElse(null);
    }

    @Override
    public GameProject create(GameProject gameProject) {
        // 设置默认值
        if (gameProject.getStatus() == null) {
            gameProject.setStatus(0);
        }
        if (gameProject.getSortOrder() == null) {
            gameProject.setSortOrder(0);
        }
        return gameProjectRepository.save(gameProject);
    }

    @Override
    public GameProject update(GameProject gameProject) {
        return gameProjectRepository.save(gameProject);
    }

    @Override
    public void delete(Long id) {
        gameProjectRepository.deleteById(id);
    }

    @Override
    public GameProject getByName(String name) {
        return gameProjectRepository.findByName(name);
    }
}
