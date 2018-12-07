package br.rede.autoclustering.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVWriter;

//import sun.invoke.empty.Empty;//deu erro qndo mudou do jre 1.8 para o 1.7
import br.rede.autoclustering.util.FitnessChart;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * @author aruanda
 *
 */
public final class Eda {

    private static Population pop;
    private static ProbDag probdag;
    private static final float ALPHA = 0.5F;
	private static final Eda eda;
    private static boolean verbose;
	
    static {
    	eda = new Eda();
    }
    
    public static Eda getInstance() {
		return eda;
	}
    
    
    public Individual execute(Collection<Node> nodes, Instances instances, Classifier classifier, int nGenerations, int nPopulation, int slices, BufferedWriter writer,int rodada, String name) throws Exception {
        Individual best = null;
    	probdag = new ProbDag(nodes, instances,classifier, slices);
    	
    	double getBestFitnessAnterior = 0;
    	double getBestFitnessAtual = 0;
    	int countChangeBest = 0;
    	String[][] logGenerations = new String[nPopulation][nGenerations]; 
    	
        for (int i = 0; i < nGenerations ; i++) {
        	if (verbose)
        		printGeneration(i);
			pop = new Population(probdag,nPopulation);
            if ((best!= null) && (pop.bestIndividual().compareTo(best) > 0))  {
                pop.addIndividual(best);
            }
            probdag.estimateProbability(pop);
            ArrayList<String> listaCombinedBlocks;// armazenar o nome dos blocos conectados
            Map<Parameter, Float> param = null;
            
            int countLine = 0;
            for (Individual ind : pop.getIndividual()){
            	listaCombinedBlocks = new ArrayList<String>();
            	String tmp = "";
            	String listaCombinedParam = "";// armazenar o nome dos parâmetros
            	
            	int sizeNodes = ind.getNodes().size();
            	int cs=0;
            	int nOfNodes = ind.getNodes().size()-1;
            	String[] saveParam = new String[nOfNodes];
                for(IndividualNode n : ind.getNodes()){
                	listaCombinedBlocks.add(n.getNode().getMethodName());
                	
                	if(cs == 1 && cs == sizeNodes-1){
						tmp = n.getNode().getMethodName();
                	}else if(cs == 1 && cs < sizeNodes-1) {
                		tmp = n.getNode().getMethodName()+"->";
					}else if(cs > 1 && cs < sizeNodes-1) {
						tmp = tmp+n.getNode().getMethodName()+"->";
					}else if(cs == sizeNodes-1) {
						tmp = tmp+n.getNode().getMethodName();
					}
                	cs++;
                	
                	if (n.getProperties().isEmpty()) {
						param = null;
					}else
					{
						param = n.getProperties();
						listaCombinedParam = listaCombinedParam+" "+param;
						/*for (int j = 0; j < saveParam.length; j++) {
						
						}*/
					}
                }
                // mostra os individuos criados para cada geração.
                //System.out.println(listaCombinedBlocks + " - fitness: " + ind.getFitness() +" | "+param);
                
                //logGenerations[countLine][i] = tmp+"->"+listaCombinedParam+"->"+ind.getFitness();
                logGenerations[countLine][i] = tmp+';'+ind.getFitness()+';'+listaCombinedParam;
                //System.out.println("logGenerations: "+logGenerations[countLine][i]);
                countLine++;
            }
            //System.out.println("//");
            
            best = pop.bestIndividual();
            ArrayList<String> lista = new ArrayList<String>();
            Map<Parameter, Float> bestParam = null;//melhor parametro utilizado no individuo
            for(IndividualNode n : best.getNodes()){
            	lista.add(n.getNode().getMethodName());
            	if (n.getProperties().isEmpty()) {
					bestParam = null;
				}else
				{
					bestParam = n.getProperties();
					// OLHAR ISSO PARA A PARTE DE PRINT DOS PARAMETROS
				}
            }
            // debug dos individuos presentes na geracao atual
            System.out.print(".");
            //System.out.println("\nRodada em andamento de número: "+(rodada+1));
            //System.out.println("Melhor individuo obtido na Geracao:" +(i+1));
            //System.out.println("Bloco(s) utilizado(s)" + lista);
            //System.out.println("Acuracia = " + best.getFitness());
            try {
				writer.write((i+1)+","+best.getFitness()+",{");
		        for (IndividualNode node : best.getNodes()){
		        	IndividualNode method = node;
		        	writer.write(method.getNode().getMethodName()+"[");
		        	Map<Parameter, Float> parameters = node.getProperties();
		        	writer.write("DISTANCE:" + node.getDistanceType());
		        	for ( Parameter p : parameters.keySet() ) {
		        		writer.write(","+p+ " - " + parameters.get(p));
		        	}
		        	writer.write("],");
		        }
		        writer.write("}\n");
		        writer.flush();
		        //writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
            
            //break if it does not change
            /*getBestFitnessAtual = best.getFitness();
            if (getBestFitnessAtual == getBestFitnessAnterior) {
				countChangeBest++;
				System.out.print(countChangeBest+" - ");
				if (countChangeBest == 50) {
					System.out.print(countChangeBest);
					System.out.println("/nStoped in Generation: "+(i+1));
					break;
				}
			}else {
				countChangeBest = 0;
				getBestFitnessAnterior = best.getFitness();
				getBestFitnessAtual = best.getFitness();
			}*/
            
            //System.gc();
        }// end FOR generations
        
        //create log about the generations
        String file = "runs/"+name+"/run-"+name+""+rodada+"/logGenerations-"+name+""+rodada+".csv";
        String[][] information = logGenerations;
        logGenerations = null;
        saveToCSV(file,information);
        
        //System.out.println("\n*****************************");
        return best;
	}

	private void saveToCSV(String file, String[][] information) {
		List<String[]> records = new ArrayList<>();
		String[] header = new String[information[0].length];
		for (int i = 0; i < header.length; i++) {
			//header[i] = "Generation_"+(i+1);
			header[i] = "B"+(i+1)+";F"+(i+1)+";P"+(i+1);
		}
		records.add(header);
		for (int i = 0; i < information.length; i++) {
			String[] data = new String[information[0].length];
			for (int j = 0; j < information[i].length; j++) {
				data[j] = information[i][j];
			}
			records.add(data);
		}

		File sp = new File(file);
        boolean exists = sp.exists();
    	if (exists) {
			System.out.println("\nFile '"+file+"' was found!");
			try /*(FileOutputStream fos = new FileOutputStream(fileName);
	                OutputStreamWriter osw = new OutputStreamWriter(fos, 
	                        StandardCharsets.UTF_8);
	                CSVWriter csvWriter = new CSVWriter(osw))*/{
				FileWriter fileWriter = new FileWriter(file);
				CSVWriter csvWriter = new CSVWriter(fileWriter,
				        //CSVWriter.DEFAULT_SEPARATOR,
						';',
				        CSVWriter.NO_QUOTE_CHARACTER,
				        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
				        CSVWriter.DEFAULT_LINE_END);
				
				csvWriter.writeAll(records);
	            fileWriter.flush();
				fileWriter.close();
	            csvWriter.flush();
	            csvWriter.close();
	            records = null;
	            //System.out.println("File '"+fileName+"' insertion COMPLETED!");
	        }catch (Exception e) {
				// TODO: handle exception
	        	//e.printStackTrace();
			}
		} else {
			System.out.println("File '"+file+"' NOT found!");
		}
	}


	/**
	 * @param totFitness
	 * @param curProb
	 * @return
	 */
	public static float pbil(double totProb, float curProb) {
		float pbil = (1 - ALPHA) *  curProb;
		pbil += ALPHA * totProb / pop.getIndividual().size()/ 2;
		return pbil;
	}

	/**
	 * @return Returns the pop.
	 */
	public Population getPop() {
		return pop;
	}
	/**
	 * @return Returns the probdag.
	 */
	public ProbDag getProbdag() {
		return probdag;
	}


	public static boolean isVerbose() {
		return verbose;
	}


	public static void setVerbose(boolean verbose) {
		Eda.verbose = verbose;
	}
	
    private void printGeneration(int i) {
    	System.out.println("******************************");
    	System.out.println("******** GENERATION: "+(i+1) + " ********");
    	System.out.println("******************************\n");
	}
}
