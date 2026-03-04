package controllers;

import src.Controller;
import src.GetMapping;
import src.ModelView;

@Controller
public class IndexController {

    @GetMapping("/index")
    public ModelView index() {
        return new ModelView("/WEB-INF/views/index.jsp");
    }
}
