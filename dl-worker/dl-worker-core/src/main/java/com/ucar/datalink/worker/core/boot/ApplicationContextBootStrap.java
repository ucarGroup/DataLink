package com.ucar.datalink.worker.core.boot;

import com.ucar.datalink.common.errors.DatalinkException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by lubiao on 2018/4/21.
 */
public class ApplicationContextBootStrap {

    private String filePath;
    private ClassPathXmlApplicationContext applicationContext;

    public ApplicationContextBootStrap(String filePath) {
        this.filePath = filePath;
    }

    public void boot() {
        try {
            applicationContext = new ClassPathXmlApplicationContext(filePath) {

                @Override
                protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
                    super.customizeBeanFactory(beanFactory);
                    beanFactory.setAllowBeanDefinitionOverriding(false);
                }
            };
        } catch (Throwable e) {
            throw new DatalinkException("ERROR ## Datalink Factory initial failed.", e);
        }
    }

    public void close() {
        if (applicationContext != null) {
            applicationContext.close();
        }
    }
}
