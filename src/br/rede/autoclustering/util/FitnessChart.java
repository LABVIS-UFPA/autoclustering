package br.rede.autoclustering.util;

import java.awt.BasicStroke;
import java.awt.Color;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class FitnessChart extends JFrame {

	private XYSeriesCollection dataset;
	private JFreeChart chart;
	private ChartPanel chartPanel;

	private XYSeries fitness = new XYSeries("Fitness");
	private XYSeries average = new XYSeries("Average");
	private double sum;
	private int num;
	private static FitnessChart fitnessChart = null;
	
	public static FitnessChart getChart(){
		if ( fitnessChart == null ) {
			fitnessChart = new FitnessChart();
			fitnessChart.setSize(1000,800);
			fitnessChart.setVisible(true);
		}
		return fitnessChart;
	}
	
	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *            the frame title.
	 */
	public FitnessChart() {
		dataset = new XYSeriesCollection();
		dataset.addSeries(fitness);
		dataset.addSeries(average);
		chart = createChart(dataset);
		chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(1000, 570));
		setContentPane(chartPanel);

	}

	public void addValue(double value){
		sum += value;
		num++;
		fitness.add(num, value);
		average.add(num,(double)sum/num);
	}
	
	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *            the data for the chart.
	 * 
	 * @return a chart.
	 */
	private JFreeChart createChart(final XYDataset dataset) {

		// create the chart...
		final JFreeChart chart = ChartFactory.createXYLineChart(
				"Fitness Chart", // chart title
				"Generation", // x axis label
				"Fitness", // y axis label
				dataset, // data
				PlotOrientation.VERTICAL, true, // include legend
				true, // tooltips
				false // urls
				);
		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		chart.setBackgroundPaint(Color.white);

		// final StandardLegend legend = (StandardLegend) chart.getLegend();
		// legend.setDisplaySeriesShapes(true);

		// get a reference to the plot for further customisation...
		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesStroke(0,  new BasicStroke(3) );
		renderer.setSeriesStroke(1,  new BasicStroke(3) );
		renderer.setSeriesLinesVisible(0, true);
		renderer.setSeriesShapesVisible(0, false);
		renderer.setSeriesLinesVisible(1, true);
		renderer.setSeriesShapesVisible(1, false);
		plot.setRenderer(renderer);

		// change the auto tick unit selection to integer units only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getDomainAxis();
		rangeAxis.setAutoRange(false);
		Range range = new Range(0.0, 100.0);
		rangeAxis.setRange(range);
		// OPTIONAL CUSTOMISATION COMPLETED.

		return chart;

	}


}
