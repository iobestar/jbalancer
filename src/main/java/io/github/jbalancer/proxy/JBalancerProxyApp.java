package io.github.jbalancer.proxy;

import io.github.jbalancer.proxy.dashboard.ABalancer;
import io.github.jbalancer.proxy.dashboard.DashboardController;
import org.springframework.stereotype.Component;
import spark.ModelAndView;
import spark.servlet.SparkApplication;
import spark.servlet.SparkFilter;
import spark.template.freemarker.FreeMarkerEngine;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.util.*;

import static spark.Spark.*;

@Component
public class JBalancerProxyApp extends SparkFilter implements SparkApplication {

    private final JsonTransformer jsonTransformer;
    private final DashboardController controller;

    public JBalancerProxyApp(DashboardController controller, JsonTransformer jsonTransformer) {
        this.controller = controller;
        this.jsonTransformer = jsonTransformer;
    }

    @Override
    public void init() {

        staticFileLocation("/public");
        redirect.get("/", "/balancers");

        // API
        get("/api/v1/node/find-all", (req, res) -> controller.getAllNodes(), jsonTransformer);
        get("/api/v1/balancer/find-all", (req, res) -> controller.getAllBalancers(), jsonTransformer);
        post("/api/v1/balancer/save", (request, response) -> {
            final ABalancer aBalancer = jsonTransformer.deserialize(request.body(), ABalancer.class);
            return controller.save(aBalancer);
        }, jsonTransformer);
        get("/api/v1/balancer/find-by-name/:name", (req, res) -> controller.getBalancer(req.params(":name")), jsonTransformer);

        // VIEWS
        get("/nodes", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return render(model, "nodes.ftl");
        });
        get("/balancers", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return render(model, "balancers.ftl");
        });
        get("/balancers/add", (req, res) -> {
            final Map<String, Object> model = new HashMap<>();
            model.put("mode", "add");
            model.put("balancerId", "");
            return render(model, "balancer.ftl");
        });
        get("/balancers/view/:id", (req, res) -> {
            final String balancerId = req.params(":id");
            final Map<String, Object> model = new HashMap<>();
            model.put("mode", "view");
            model.put("balancerId", balancerId);
            return render(model, "balancer.ftl");
        });
    }

    private static String render(Map<String, Object> model, String templatePath) {
        return new FreeMarkerEngine().render(new ModelAndView(model, templatePath));
    }

    @Override
    protected SparkApplication[] getApplications(FilterConfig filterConfig) throws ServletException {

        final List<SparkApplication> applications = new ArrayList<>(Arrays.asList(Optional.ofNullable(super.getApplications(filterConfig)).orElse(new SparkApplication[0])));
        applications.add(this);
        return applications.toArray(new SparkApplication[applications.size()]);
    }
}
