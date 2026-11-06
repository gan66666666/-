package com.pet.adopt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.adopt.dto.StatsOverviewDTO;
import com.pet.adopt.dto.StatsPetTypeDTO;
import com.pet.adopt.dto.StatsTrendDTO;
import com.pet.adopt.entity.AdoptApply;
import com.pet.adopt.entity.AdoptRecord;
import com.pet.adopt.entity.Pet;
import com.pet.adopt.exception.BusinessException;
import com.pet.adopt.mapper.AdoptApplyMapper;
import com.pet.adopt.mapper.AdoptRecordMapper;
import com.pet.adopt.mapper.PetMapper;
import com.pet.adopt.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作台统计服务实现
 * 所有统计口径都放在本层，Controller 只做参数接收与结果包装
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    /** 近7天（含今天） */
    private static final int WEEK_DAYS = 7;

    /** 近30天（含今天） */
    private static final int MONTH_DAYS = 30;

    /** 近12个月（含本月） */
    private static final int YEAR_MONTHS = 12;

    /** 趋势日期格式：yyyy-MM-dd */
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 趋势日期格式：yyyy-MM */
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /** 宠物类型固定展示顺序 */
    private static final List<String> PET_TYPE_ORDER = List.of("狗狗", "猫咪", "兔子", "其他");

    private final AdoptApplyMapper adoptApplyMapper;

    private final AdoptRecordMapper adoptRecordMapper;

    private final PetMapper petMapper;

    @Override
    public StatsOverviewDTO getOverview() {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        // 待审核申请数：adopt_apply.status = 0
        Long pendingApply = adoptApplyMapper.selectCount(
                new LambdaQueryWrapper<AdoptApply>().eq(AdoptApply::getStatus, 0));

        // 可领养宠物数：pet.status = 1
        Long adoptablePet = petMapper.selectCount(
                new LambdaQueryWrapper<Pet>().eq(Pet::getStatus, 1));

        // 今日新增申请数：apply_time 落在今天 [00:00, 次日00:00)
        Long todayApply = adoptApplyMapper.selectCount(
                new LambdaQueryWrapper<AdoptApply>()
                        .ge(AdoptApply::getApplyTime, today.atStartOfDay())
                        .lt(AdoptApply::getApplyTime, today.plusDays(1).atStartOfDay()));

        // 本月成功领养数：adopt_date 落在本月 [月初, 下月初)
        Long monthAdopt = adoptRecordMapper.selectCount(
                new LambdaQueryWrapper<AdoptRecord>()
                        .ge(AdoptRecord::getAdoptDate, currentMonth.atDay(1))
                        .lt(AdoptRecord::getAdoptDate, currentMonth.plusMonths(1).atDay(1)));

        StatsOverviewDTO overview = new StatsOverviewDTO();
        overview.setPendingApply(pendingApply);
        overview.setAdoptablePet(adoptablePet);
        overview.setTodayApply(todayApply);
        overview.setMonthAdopt(monthAdopt);
        return overview;
    }

    @Override
    public List<StatsTrendDTO> getTrend(String type) {
        String dimension = type == null ? "week" : type.trim().toLowerCase();
        LocalDate today = LocalDate.now();
        switch (dimension) {
            case "week":
                return buildDayTrend(today.minusDays(WEEK_DAYS - 1L), today, WEEK_DAYS);
            case "month":
                return buildDayTrend(today.minusDays(MONTH_DAYS - 1L), today, MONTH_DAYS);
            case "year":
                return buildMonthTrend(today);
            default:
                throw new BusinessException("统计类型只能是week/month/year");
        }
    }

    @Override
    public List<StatsPetTypeDTO> getPetTypeDistribution() {
        // 只取 type 一列，减少无效字段的读取
        List<Pet> pets = petMapper.selectList(
                new LambdaQueryWrapper<Pet>().select(Pet::getType));

        Map<String, Long> counter = new LinkedHashMap<>();
        for (String petType : PET_TYPE_ORDER) {
            counter.put(petType, 0L);
        }
        for (Pet pet : pets) {
            String petType = pet.getType();
            if (petType == null) {
                continue;
            }
            counter.merge(petType, 1L, Long::sum);
        }

        List<StatsPetTypeDTO> result = new ArrayList<>(counter.size());
        for (Map.Entry<String, Long> entry : counter.entrySet()) {
            result.add(new StatsPetTypeDTO(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    /**
     * 构建按天统计的趋势：生成连续日期，无数据的日期补 count=0
     */
    private List<StatsTrendDTO> buildDayTrend(LocalDate start, LocalDate end, int days) {
        Map<String, Long> counter = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            counter.put(start.plusDays(i).format(DAY_FORMATTER), 0L);
        }

        List<AdoptRecord> records = adoptRecordMapper.selectList(
                new LambdaQueryWrapper<AdoptRecord>()
                        .ge(AdoptRecord::getAdoptDate, start)
                        .le(AdoptRecord::getAdoptDate, end)
                        .select(AdoptRecord::getAdoptDate));
        for (AdoptRecord record : records) {
            if (record.getAdoptDate() == null) {
                continue;
            }
            String key = record.getAdoptDate().format(DAY_FORMATTER);
            counter.merge(key, 1L, Long::sum);
        }

        List<StatsTrendDTO> result = new ArrayList<>(counter.size());
        for (Map.Entry<String, Long> entry : counter.entrySet()) {
            result.add(new StatsTrendDTO(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    /**
     * 构建按月统计的趋势：生成连续月份，无数据的月份补 count=0
     */
    private List<StatsTrendDTO> buildMonthTrend(LocalDate today) {
        YearMonth current = YearMonth.from(today);
        YearMonth start = current.minusMonths(YEAR_MONTHS - 1L);

        Map<String, Long> counter = new LinkedHashMap<>();
        for (int i = 0; i < YEAR_MONTHS; i++) {
            counter.put(start.plusMonths(i).format(MONTH_FORMATTER), 0L);
        }

        List<AdoptRecord> records = adoptRecordMapper.selectList(
                new LambdaQueryWrapper<AdoptRecord>()
                        .ge(AdoptRecord::getAdoptDate, start.atDay(1))
                        .le(AdoptRecord::getAdoptDate, current.atEndOfMonth())
                        .select(AdoptRecord::getAdoptDate));
        for (AdoptRecord record : records) {
            if (record.getAdoptDate() == null) {
                continue;
            }
            String key = YearMonth.from(record.getAdoptDate()).format(MONTH_FORMATTER);
            counter.merge(key, 1L, Long::sum);
        }

        List<StatsTrendDTO> result = new ArrayList<>(counter.size());
        for (Map.Entry<String, Long> entry : counter.entrySet()) {
            result.add(new StatsTrendDTO(entry.getKey(), entry.getValue()));
        }
        return result;
    }
}
