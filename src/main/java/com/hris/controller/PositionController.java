package com.hris.controller;

import com.hris.model.Position;
import com.hris.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Position Controller
 * Handles position management pages and API endpoints
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/positions")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class PositionController {

    private final PositionService positionService;

    // =====================================================
    // PAGE CONTROLLERS
    // =====================================================

    /**
     * Position list page (with pagination and filtering)
     */
    @GetMapping
    public String listPositions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer level,
            Model model) {
        log.info("Loading position list page - page: {}, size: {}, search: {}, level: {}", page, size, search, level);

        // Create pageable with sorting
        Pageable pageable = PageRequest.of(page, size, Sort.by("level").ascending().and(Sort.by("name").ascending()));

        // Get paginated and filtered positions
        Page<Position> positionPage = positionService.searchPositions(search, level, pageable);

        model.addAttribute("activePage", "positions");
        model.addAttribute("positionPage", positionPage);
        model.addAttribute("positions", positionPage.getContent());

        // Filter parameters
        model.addAttribute("search", search);
        model.addAttribute("level", level);

        // Pagination info
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", positionPage.getTotalPages());
        model.addAttribute("totalItems", positionPage.getTotalElements());
        model.addAttribute("pageSize", size);

        // Stats - calculate counts by level
        long level1Count = positionPage.getContent().stream().filter(p -> p.getLevel() == 1).count();
        long level4Count = positionPage.getContent().stream().filter(p -> p.getLevel() == 4).count();
        long executiveCount = positionPage.getContent().stream().filter(p -> p.getLevel() >= 5).count();

        model.addAttribute("totalPositions", (int) positionPage.getTotalElements());
        model.addAttribute("level1Count", (int) level1Count);
        model.addAttribute("level4Count", (int) level4Count);
        model.addAttribute("executiveCount", (int) executiveCount);
        model.addAttribute("levelOptions", new int[]{1, 2, 3, 4, 5, 6});

        return "position/list";
    }

    /**
     * Position create form page
     */
    @GetMapping("/create")
    public String createPositionForm(Model model) {
        log.info("Loading position create form");

        model.addAttribute("activePage", "positions");
        model.addAttribute("position", new Position());
        model.addAttribute("isEdit", false);

        return "position/form";
    }

    /**
     * Position edit form page
     */
    @GetMapping("/{id}/edit")
    public String editPositionForm(@PathVariable Long id, Model model) {
        log.info("Loading position edit form for ID: {}", id);

        Position position = positionService.getPositionById(id);
        if (position == null) {
            return "redirect:/positions";
        }

        model.addAttribute("activePage", "positions");
        model.addAttribute("position", position);
        model.addAttribute("isEdit", true);

        return "position/form";
    }

    // =====================================================
    // FORM SUBMISSION HANDLERS
    // =====================================================

    /**
     * Create new position
     */
    @PostMapping("/create")
    public String createPosition(@Valid @ModelAttribute Position position,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        log.info("Creating position: {}", position.getName());

        // Validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("position", position);
            model.addAttribute("isEdit", false);
            return "position/form";
        }

        try {
            Position saved = positionService.createPosition(position);
            redirectAttributes.addFlashAttribute("success", "Data berhasil disimpan");
            return "redirect:/positions";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("position", position);
            model.addAttribute("isEdit", false);
            return "position/form";
        }
    }

    /**
     * Update existing position
     */
    @PostMapping("/{id}/edit")
    public String updatePosition(@PathVariable Long id,
                                @Valid @ModelAttribute Position position,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        log.info("Updating position ID: {}", id);

        // Validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("position", position);
            model.addAttribute("isEdit", true);
            return "position/form";
        }

        try {
            Position updated = positionService.updatePosition(id, position);
            redirectAttributes.addFlashAttribute("success", "Data berhasil disimpan");
            return "redirect:/positions";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("position", position);
            model.addAttribute("isEdit", true);
            return "position/form";
        }
    }

    /**
     * Delete position
     */
    @PostMapping("/{id}/delete")
    public String deletePosition(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        log.info("Deleting position ID: {}", id);

        try {
            positionService.deletePosition(id);
            redirectAttributes.addFlashAttribute("success", "Data berhasil dihapus");
        } catch (IllegalArgumentException e) {
            log.error("Error deleting position: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/positions";
    }
}
