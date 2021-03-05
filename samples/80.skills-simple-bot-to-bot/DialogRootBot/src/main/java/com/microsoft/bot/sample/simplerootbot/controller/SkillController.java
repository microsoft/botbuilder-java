package com.microsoft.bot.sample.simplerootbot.controller;

import com.microsoft.bot.builder.ChannelServiceHandler;
import com.microsoft.bot.integration.spring.ChannelServiceController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/api/skills"})
public class SkillController extends ChannelServiceController {

    public SkillController(ChannelServiceHandler handler) {
        super(handler);
    }
}
