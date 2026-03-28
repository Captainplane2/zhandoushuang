package com.esports.zds;

import com.esports.zds.entity.GameProject;
import com.esports.zds.repository.GameProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 游戏板块数据初始化类
 */
@Component
public class GameProjectInitializer implements CommandLineRunner {

    @Autowired
    private GameProjectRepository gameProjectRepository;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有游戏板块数据
        List<GameProject> existingProjects = gameProjectRepository.findAll();
        if (existingProjects.isEmpty()) {
            // 初始化默认游戏板块
            initializeDefaultGameProjects();
        }
    }

    private void initializeDefaultGameProjects() {
        // 创建CS2板块
        GameProject cs2 = new GameProject();
        cs2.setName("CS2");
        cs2.setDisplayName("CS2");
        cs2.setSortOrder(1);
        cs2.setStatus(0);
        gameProjectRepository.save(cs2);

        // 创建LOL板块
        GameProject lol = new GameProject();
        lol.setName("LOL");
        lol.setDisplayName("英雄联盟");
        lol.setSortOrder(2);
        lol.setStatus(0);
        gameProjectRepository.save(lol);

        // 创建王者荣耀板块
        GameProject wzry = new GameProject();
        wzry.setName("WZRY");
        wzry.setDisplayName("王者荣耀");
        wzry.setSortOrder(3);
        wzry.setStatus(0);
        gameProjectRepository.save(wzry);

        System.out.println("默认游戏板块初始化完成");
    }
}
