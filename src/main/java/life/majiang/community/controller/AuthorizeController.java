package life.majiang.community.controller;

import life.majiang.community.dto.AccessTokenDTO;
import life.majiang.community.dto.GithubUser;
import life.majiang.community.mapper.UserMapper;
import life.majiang.community.model.User;
import life.majiang.community.provider.GithubProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * 授权登录
 */
@Controller
public class AuthorizeController {
    @Autowired
    private GithubProvider githubProvider;

    @Value("${github.client.id}")
    private  String clientId;
    @Value("${github.client.secret}")
    private  String clientSecret;
    @Value("${github.redirect.uri}")
    private  String redirectUri;

    @Autowired
    private UserMapper userMapper;
    @GetMapping("/callback")
    public String callback(@RequestParam(name = "code") String code, @RequestParam(name="state") String state, HttpServletRequest request, HttpServletResponse response){
        AccessTokenDTO accessTokenDTO =new AccessTokenDTO();
        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setCode(code);
        accessTokenDTO.setState(state);
        accessTokenDTO.setRedirect_uri(redirectUri);
        accessTokenDTO.setClient_secret(clientSecret);
        String access_token=githubProvider.getAccessToken(accessTokenDTO);
        GithubUser githubUser=githubProvider.getUseer(access_token);
        //登录成功
        if(githubUser!=null){
            //创建user对象
            User user =new User();
            //定义token
            String token= (UUID.randomUUID().toString());
            //将属性设置到user中
            user.setToken(token);
            user.setAccountId(String.valueOf(githubUser.getId()));
            user.setGmtCreated(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreated());
            user.setName(githubUser.getName());
            //将user插入数据库中
            userMapper.insert(user);
            //同时设置cookie-token属性
            response.addCookie(new Cookie("token",token));
            return "redirect:/";
        }else {
            //登录失败，重新登录
            return "redirect:/";
        }

    }

}
