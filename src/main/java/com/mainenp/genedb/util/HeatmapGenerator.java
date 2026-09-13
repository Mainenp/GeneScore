package com.mainenp.genedb.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class HeatmapGenerator {

    public static String generateHeatmapBase64(List<String> genes, Map<String, Map<String, Double>> scores) {
        try {
            // 创建数据集
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (String gene1 : genes) {
                for (String gene2 : genes) {
                    Double score = scores.get(gene1) != null ? scores.get(gene1).get(gene2) : 0.0;
                    dataset.addValue(score != null ? score : 0.0, gene1, gene2);
                }
            }

            // 创建条形图（作为替代方案）
            JFreeChart chart = ChartFactory.createBarChart(
                    "Gene Synergy Heatmap",
                    "Target Gene",
                    "Synergy Score",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            // 配置样式
            chart.setBackgroundPaint(Color.WHITE);
            chart.getTitle().setPaint(new Color(51, 51, 51));
            chart.getTitle().setFont(new Font("Arial", Font.BOLD, 18));

            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinePaint(new Color(200, 200, 200));
            plot.setRangeGridlinePaint(new Color(200, 200, 200));

            // 设置轴标签旋转
            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
            domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 12));
            domainAxis.setLabelFont(new Font("Arial", Font.BOLD, 14));

            // 设置值轴范围
            NumberAxis valueAxis = (NumberAxis) plot.getRangeAxis();
            valueAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 12));
            valueAxis.setLabelFont(new Font("Arial", Font.BOLD, 14));
            valueAxis.setRange(-2.0, 2.0);

            // 自定义条形颜色
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setDefaultItemLabelFont(new Font("Arial", Font.PLAIN, 10));

            // 转换为Base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(outputStream, chart, 1000, 800);
            byte[] imageBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
