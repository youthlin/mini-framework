package com.youthlin.mvc.support.thymeleaf;

import com.youthlin.ioc.annotaion.AnnotationUtil;
import com.youthlin.mvc.annotation.ResponseBody;
import com.youthlin.mvc.listener.ControllerAndMethod;
import com.youthlin.mvc.servlet.Constants;
import com.youthlin.mvc.support.View;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-18 14:31.
 */
@Resource
public class ThymeleafView implements View {
    private TemplateEngine templateEngine;

    private void init(ServletContext servletContext) {
        ServletContextTemplateResolver resolver = new ServletContextTemplateResolver(servletContext);
        resolver.setTemplateMode(TemplateMode.HTML);
        String prefix = servletContext.getInitParameter(Constants.TH_VIEW_PREFIX);
        if (prefix == null) {
            prefix = servletContext.getInitParameter(Constants.VIEW_PREFIX_PARAM_NAME);
        }
        if (prefix == null) {
            prefix = "";// /WEB-INF/templates/
        }
        String suffix = servletContext.getInitParameter(Constants.TH_VIEW_SUFFIX);
        if (suffix == null) {
            suffix = servletContext.getInitParameter(Constants.VIEW_SUFFIX_PARAM_NAME);
        }
        if (suffix == null) {
            suffix = "";// .html
        }
        resolver.setPrefix(prefix);
        resolver.setSuffix(suffix);
        resolver.setCacheTTLMs(3600000L);
        resolver.setCacheable(true);
        resolver.setCharacterEncoding("UTF-8");
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
    }

    @Override
    public boolean render(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model, Object result, ControllerAndMethod controllerAndMethod) throws Exception {
        if (templateEngine == null) {
            init(request.getServletContext());
        }
        if (AnnotationUtil.getAnnotation(controllerAndMethod.getMethod(), ResponseBody.class) != null
                || !(result instanceof String)) {
            return false;//不处理 ResponseBody
        }
        WebContext ctx = new WebContext(request, response, request.getServletContext(), request.getLocale(), model);
        templateEngine.process((String) result, ctx, response.getWriter());
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    // allow user to custom settings
    public ThymeleafView setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        return this;
    }
}
