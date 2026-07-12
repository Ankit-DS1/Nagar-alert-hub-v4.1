package com.nagaralert.controller;

import com.nagaralert.model.AppUser;

import com.nagaralert.repository.AppUserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final AppUserRepository appUserRepository;

    public AuthController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }



    @GetMapping("/require-phone")
    public String requirePhonePage(@AuthenticationPrincipal org.springframework.security.oauth2.core.user.OAuth2User oauthUser, Model model) {
        if (oauthUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("name", oauthUser.getAttribute("name"));
        return "require-phone";
    }

    @PostMapping("/submit-phone")
    public String submitPhone(@RequestParam("mobileNumber") String mobileNumber, 
                              org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken) {
        if (oauthToken != null) {
            org.springframework.security.oauth2.core.user.OAuth2User oauthUser = oauthToken.getPrincipal();
            String provider = oauthToken.getAuthorizedClientRegistrationId().toUpperCase();
            
            String oauthId = oauthUser.getName();
            if (provider.equals("GOOGLE") && oauthUser.getAttribute("sub") != null) {
                oauthId = oauthUser.getAttribute("sub");
            } else if (oauthUser.getAttribute("id") != null) {
                oauthId = oauthUser.getAttribute("id").toString();
            }
            
            AppUser appUser = appUserRepository.findByOauthIdAndOauthProvider(oauthId, provider).orElse(null);
            if (appUser != null) {
                appUser.setMobileNumber(mobileNumber);
                appUserRepository.save(appUser);
            }
        }
        return "redirect:/";
    }
}
