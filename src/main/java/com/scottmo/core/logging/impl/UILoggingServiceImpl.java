package com.scottmo.core.logging.impl;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.scottmo.core.logging.api.LoggingService;

public class UILoggingServiceImpl implements LoggingService {
    private final Logger logger = Logger.getLogger(LoggingService.class.getName());
    private final JFrame frame;

    public UILoggingServiceImpl(JFrame frame) {
        this.frame = frame;
    }

    @Override
    public void info(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "INFO", JOptionPane.INFORMATION_MESSAGE);
    }
    @Override
    public void warn(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "WARNING", JOptionPane.WARNING_MESSAGE);
    }
    @Override
    public void error(String msg, Throwable e) {
        JOptionPane.showMessageDialog(frame, msg, "ERROR", JOptionPane.ERROR_MESSAGE);
        logger.error(msg, e);
    }
}
