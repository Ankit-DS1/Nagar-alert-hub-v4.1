package com.nagaralert.controller;

import com.nagaralert.model.AppUser;
import com.nagaralert.repository.AppUserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class ProfileController {

    private final AppUserRepository appUserRepository;

    public ProfileController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal OAuth2User oauth2User, OAuth2AuthenticationToken auth, Model model) {
        if (oauth2User == null) {
            return "redirect:/login"; // Only accessible to logged-in OAuth citizens
        }

        String provider = auth.getAuthorizedClientRegistrationId().toUpperCase();
        String oauthId = oauth2User.getName();
        if (provider.equals("GOOGLE") && oauth2User.getAttribute("sub") != null) {
            oauthId = oauth2User.getAttribute("sub");
        } else if (oauth2User.getAttribute("id") != null) {
            oauthId = oauth2User.getAttribute("id").toString();
        }

        Optional<AppUser> appUserOpt = appUserRepository.findByOauthIdAndOauthProvider(oauthId, provider);
        
        if (appUserOpt.isPresent()) {
            model.addAttribute("appUser", appUserOpt.get());
        } else {
            return "redirect:/login";
        }

        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal OAuth2User oauth2User, OAuth2AuthenticationToken auth,
                                @RequestParam String dob, @RequestParam String mobileNumber) {
        if (oauth2User == null) {
            return "redirect:/login";
        }

        String provider = auth.getAuthorizedClientRegistrationId().toUpperCase();
        String oauthId = oauth2User.getName();
        if (provider.equals("GOOGLE") && oauth2User.getAttribute("sub") != null) {
            oauthId = oauth2User.getAttribute("sub");
        } else if (oauth2User.getAttribute("id") != null) {
            oauthId = oauth2User.getAttribute("id").toString();
        }

        Optional<AppUser> appUserOpt = appUserRepository.findByOauthIdAndOauthProvider(oauthId, provider);
        if (appUserOpt.isPresent()) {
            AppUser appUser = appUserOpt.get();
            appUser.setDob(dob);
            appUser.setMobileNumber(mobileNumber);
            appUserRepository.save(appUser);
        }

        return "redirect:/profile?success";
    }
}
