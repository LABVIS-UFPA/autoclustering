package br.rede.autoclustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opencsv.CSVWriter;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Eda;
import br.rede.autoclustering.core.Edge;
import br.rede.autoclustering.core.Individual;
import br.rede.autoclustering.core.IndividualNode;
import br.rede.autoclustering.core.Node;
import br.rede.autoclustering.savedata.SaveData;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.util.ClusterViewerFrame;
import br.rede.autoclustering.util.DataBaseStructure;
import br.rede.autoclustering.util.ParameterOptions;
import br.rede.autoclustering.vo.AutoClusteringVO;
import br.rede.autoclustering.vo.EdgeVO;
import br.rede.autoclustering.vo.NodeVO;
import br.rede.autoclustering.vo.ParameterVO;
import br.rede.autoclustering.vo.VOLoad;

import java.util.Random;
import java.util.Set;

public class Launcher {
   
    public static void main(String[] args) throws Exception {
           int runs = 30;//30
           int generations = 500;//500
           int population = 50;//50
           int slices = 10;
           int sigma = 2;
           float epsilon = 0.3f;
           Classifier classifier = new J48();
           //File input = new File("databases/sinteticDataXY_2.arff");//
           //File input = new File("databases/dbclasd.in");
           
           //File input = new File("uci-databases/heart-disease/processed.cleveland.data"); //
           //File input = new File("uci-databases/glass/glass2.arff"); //ok
           //File input = new File("uci-databases/pima-indians-diabetes/pima-indians-diabetes.data");// ok, mas lento
           //File input = new File("uci-databases/liver-disorders/bupa.data");// ok
           //File input = new File("databases/synthetic-1.arff");// ok
           File input = new File("databases/synthetic-2_processed.arff");// ok
           //File input = new File("databases/synthetic-3.arff");// ok
           
           new Launcher(input, classifier, runs, generations, population, slices, sigma, epsilon, false);
        }
   
    public String getUseNetwork() throws UnknownHostException{
        InetAddress addr = InetAddress.getLocalHost();
        return addr.getHostName();
    }

    @SuppressWarnings("finally")
    public String escrever(String endereco, String nomeDoArquivo, StringBuilder builder) {
        String msg = "";
        try {
            File file = new File(endereco + File.separator + nomeDoArquivo);
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw); // BufferedWriter
            bw.write(builder.toString()); // Inserção bufferizada da String texto1 no arquivo file.txt
            bw.flush();
            bw.close();
            msg = "Informações adicionadas com sucesso ao arquivo "+file.getAbsoluteFile();
        } catch (IOException e) {
            msg = "Erro de IO: Falha ao escrever arquivo: " + e.getMessage();
            System.err.println("Falha ao escrever arquivo: " + e.getMessage());
        }finally{
            return msg;
        }
    }
   
    public Launcher(File file3, Classifier classifier, int runs, int generations, int population, int slices, int dbSigma, float dbEpsilon, boolean verbose) {
        Collection<Node> nodes = null;
        String name = file3.getName();
        
        try {
            nodes = loadConfig(slices);
        } catch (Exception e1) {
            System.err.println("There was a problem loading configuration files. Please, check your config.xml");
            e1.printStackTrace();
        }
        StringBuffer defaultFolder = new StringBuffer("runs/"+name+"/");
        Eda.setVerbose(verbose);
        BufferedWriter edaGenerationRuns = null, history = null;
       
        try {
            BufferedReader reader = new BufferedReader(new FileReader("runs/"+name+"/summary-"+name+".csv"));
            String header = reader.readLine();
           
            edaGenerationRuns = buildWriter(defaultFolder, new StringBuffer("eda-"+name), ".log");
            history = buildWriter(defaultFolder, new StringBuffer("eda-summary-"+name), ".csv");
            history.write(header+",eda\n");
            StringBuilder builder = new StringBuilder();
            
            // calcular tempo de execução geral
            long started = System.currentTimeMillis();
            
            //for (int j = 18; j < runs; j++) {// usar quando interrompido
           for (int j = 0; j < runs; j++) {
        	    long runStarted = System.currentTimeMillis();
                System.out.println("Lendo '" +name + "' e executando a particao "+j+" na maquina "+getUseNetwork()+". ");
                builder.append("Algoritmo: EDA FULL \r\n");
                builder.append("Lendo '" +name + "' e executando a particao "+j+" na maquina "+getUseNetwork()+".\r\n");
                //System.out.println("Lendo " +name + " e executando a partição "+j);
                
                File files = new File("runs/"+name+"/run-"+name+j+"/dbscan-run-"+name+j+"-train.arff");
                DataBaseStructure db = new DataBaseStructure();
                    db.loadDataBase(files);
                   
                    DataBaseStructure db2 = new DataBaseStructure();
                    db2.loadDataBase(new File("runs/"+name+"/run-"+name+j+"/dbscan-run-"+name+j+"-test.arff"));
                   
                    Instances train = db.getDatabase();//original
                    //Instances train = db.getNormalizedData();
                    train.deleteAttributeAt(train.numAttributes()-1);
                    
                    Instances test = db2.getDatabase();//original
                    //Instances test = db2.getNormalizedData();
                    test.deleteAttributeAt(test.numAttributes()-1);
                //folder
//                StringBuffer folder = new StringBuffer(defaultFolder);
//                folder.append("run-").append(out).append(j);
//                mkdir(folder.toString());
//                //eda
//                StringBuffer eda = new StringBuffer("eda-run-");
//                eda.append(out).append(j);
//                //dbscan
//                StringBuffer dbscan = new StringBuffer("dbscan-run-");
//                dbscan.append(out).append(j);

//                edaGenerationRuns = buildWriter(folder,eda,".txt");
//                edaTrain = buildWriter(folder,eda,"-train.arff");
//                edaTest = buildWriter(folder,eda,"-test.arff");
                String currentPartition = "runs/"+name+"/run-"+name+j+"";
                
                
                int rod = j;//rodada
                System.out.println("Rodada de numero: "+(rod+1));
                double edaFit = runEDA(nodes, train, classifier, generations, population, slices, test,
                		edaGenerationRuns, rod,name,builder,currentPartition);
                //reduzir o n'umero de casas decimais
                BigDecimal bdf = new BigDecimal(edaFit).setScale(4, RoundingMode.HALF_EVEN);
                double edaFit2 = bdf.doubleValue();
                
                
                //System.out.println("Number of Instances on Training: "+train.numInstances());
                //System.out.println("Number of Instances on Testing: "+test.numInstances());
                
                //System.out.println("Number of Attributes: "+train.numAttributes());
                //System.out.print("Attributes: ");
//                for (int i = 0; i < train.numAttributes(); i++) {
//                	System.out.print(train.attribute(i)+" | ");
//				}
                //System.out.println("\n");
                //System.out.println("Graph's nodes: "+nodes);
                System.out.println("Melhor fitness das "+generations+" gerações: "+edaFit2);
                //builder.append("Melhor fitness das "+generations+" gerações: "+edaFit2+".\r\n\r\n");
                history.write(reader.readLine()+","+String.valueOf(edaFit2));
                history.write("\n");
                history.flush();
                long diff = System.currentTimeMillis() - runStarted;
                builder.append("Tempo de processamento da rodada: "+String.format("%5d", (diff / 60000)%60)+"(min) e "+String.format("%2d", (diff / 1000)%60)+" (s)\r\n");
                builder.append("Tempo de processamento da rodada: "+(diff / 1000)+"(s)\r\n\r\n");
                //String fileName = "EDA_"+name+"_"+j+".txt";
                String fileName = "EDA_"+name+"_resumo.txt";
                String msg = escrever(System.getProperty("user.dir"), fileName, builder);
                System.out.println(msg+"\n");
                
                /*File analysisFile = new File("runs/"+name+"/run-"+name+"0/logGenerations-"+name+"0.csv");
                File currentPath = new File("runs/"+name+"/run-"+name+""+j+"/logGenerations-"+name+""+j+".csv");
                File directory = new File("runs/"+name+"/run-"+name+""+j+"/logAlgorithms");
                if (analysisFile.compareTo(currentPath) == 0) {
					System.out.println("Aqui estou para fazer a bagaça rodar!");
					SaveData writeCSV = new SaveData();
					writeCSV.saveParameterPerAlgorithm(analysisFile,directory);
					
				} else {
					System.out.println("Faço nada aqui!");
				}*/
                
                System.out.println("\n*****************************");
            }//end FOR runs
            
            long elapsed = System.currentTimeMillis() - started;
            //builder.append("Tempo total de processamento: "+String.format("%3d", (elapsed / 86400000))+" dias\r\n");
            //builder.append("Tempo total de processamento: "+String.format("%2d", (elapsed / 3600000)%24)+" horas\r\n");
            builder.append("\r\nTempo total de processamento: "+String.format("%6d", (elapsed / 60000))+"(min) e "+String.format("%2d", (elapsed / 1000)%60)+"\r\n");
            builder.append("Tempo total de processamento: "+(elapsed / 1000)+"(s)\r\n");
            //builder.append("Tempo total de processamento: "+String.format("%2d", (elapsed / 1000)%60)+" segundos\r\n");
            String fileName = "EDA_"+name+"_resumo.txt";
            String msg = escrever(System.getProperty("user.dir"), fileName, builder);
 			System.out.println("Tempo total de processamento: "+(elapsed/60000)+"(min) e "+((elapsed/1000)%60)+"(s).");
            history.close();
            
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
   
    private void mkdir(String string) {
        new File(string).mkdir();
    }


    private double runEDA(Collection<Node> nodes, Instances train, Classifier classifier, int generations, int population,
    		int slices, Instances test, BufferedWriter writer,int rodada,String name,StringBuilder builder, String currentPartition){
        Individual best = null;
        try {
            best = Eda.getInstance().execute(nodes, train, classifier, generations, population,slices,writer,rodada,name);// adicionado a rodada atual
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        Instances instances = best.getInstances();

        //System.out.println("Print do instance depois do try/catch"+instances);

        //Debug
       // System.out.println("#################");
       // System.out.println("best.getGroups(): "+best.getGroups());
       // System.out.println("best.getGroups().isEmpty(): "+best.getGroups().isEmpty());
       // System.out.println("best.getGroups().size(): "+best.getGroups().size());
       // for (int i = 0; i < best.getGroups().size(); i++) {
			//System.out.println("Tam GP:"+best.getGroups().get(i).getInstances().size());
		//}
        //System.out.println("#################");
        ///////////////////////////////////////////////////////////////////////////////
        
        if ( best.getGroups() != null && best.getGroups().isEmpty() && best.getGroups().size() != 1) {
        //if ( best.getGroups() != null && best.getGroups().isEmpty() && best.getGroups().size() >= 1) {
            Attribute cluster = createAttribute(best.getGroups().size());
            trainDatabase(best, cluster);

            buildClassifier(classifier, instances);
            classify(classifier, test, cluster);
       
//            writeDataSet(edaTest, test);
//            writeDataSet(edaTrain, best.getInstances());
       
            cleanInstances(best.getInstances());
            cleanInstances(test);
        }else
        {
        	//System.out.println("NÃO entrei no IF do createAttribute()");
        }
        
        // Save properties on file
        //StringBuilder builder = new StringBuilder();
        builder.append("Configuração do melhor indivíduo:\r\n");
        builder.append("Número de grupos identificados: "+best.getGroups().size()+"\r\n");
        System.out.println("\nMelhor individuo da rodada '"+(rodada+1)+"'");
        System.out.println("N. of groups: "+best.getGroups().size());
        System.out.println("Evaluate(CV-SSE): "+best.evaluate(instances, best.getGroups()));
        
        	/*new ClusterViewerFrame(instances, best.getGroups());
        	try {
    			Thread.sleep(10000);
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}*/
        
        for (IndividualNode n : best.getNodes()){
        	System.out.println(n.getNode().getMethodName()+" - "+n.getProperties());
        	builder.append(n.getNode().getMethodName()+" - "+n.getProperties()+"\r\n");
        	//System.out.print(n.getProperties());
        }
        //String fileName = "EDA_"+name+"_resumo.txt";
        //String msg = escrever(System.getProperty("user.dir"), fileName, builder);
        //System.out.println(msg+"\n");
        System.out.println("Acurácia do indivíduo: "+best.getFitness());
        builder.append("Acurária do indivíduo: "+best.getFitness()+"\r\n");
        //System.out.println("");
        
        //File tmpDir = new File("runs/"+name+"/summary-"+name+".csv");
        //String diretorio = "runs/"+name+"";
        //File tmpDir = new File(diretorio);
        //boolean exists = tmpDir.exists();
        //System.out.println("Diretorio atual: "+currentPartition);
        //File sp = new File(""+currentPartition+"/1-preSilhouette.csv");
        //boolean exists = sp.exists();
        //System.out.println("Arquivo '1-preSilhouette.csv' existe: "+exists);
        //write the groups on CSV file
        String sFile = currentPartition+"/1-preSilhouette.csv";
        writeByOpenCSV(sFile,best.getGroups(),instances);//em análise
        
        return best.getFitness();
    }
    
    //using the openCSV method
	private void writeByOpenCSV(String fileName, List<Group> list, Instances instances){
		// TODO Auto-generated method stub
    	int nGroups = list.size();
    	int nAtt = instances.numAttributes();
    	
    	String tmp = "";
    	for (int i = 1; i <= nAtt; i++) {
			if (i != nAtt) {
				tmp = tmp.concat("Attribute"+i+",");
			}else {
				tmp = tmp.concat("Attribute"+i+",Cluster");
			}
		}
    	String[] header = tmp.split(",");
    	
    	//String[] data = {};
    	List<String[]> records = new ArrayList<>();
    	records.add(header);
    	int i = 0;
    	for (Group group : list) {
    		//System.out.println("Tamanho do grupo '"+i+"': "+group.getInstances().size());
    		//System.out.println("group " + i + ":" + group.getInstances().size());
			i++;
    		Set<Instance> nInstances = group.getInstances();//get instances of the group
    		//System.out.println("Group '"+i+"' - size: '"+group.getInstances().size()+"'");
    		//System.out.println(nInstances);
    		Object[] p = nInstances.toArray();
    		for (int j = 0; j < p.length; j++) {
    			String a = p[j].toString()+",C"+i;
    			String[] data = a.split(",");
    			records.add(data);
				//System.out.print(b[2]+" | ");
			}//j
    	}//foreach
    	
    	/*//debug
    	System.out.println("Print List Records:");
    	for (int j = 0; j < records.size(); j++) {
			System.out.println(Arrays.toString(records.get(j)));
		}*/
    	
    	
    	File sp = new File(fileName);
        boolean exists = sp.exists();
    	if (exists) {
			System.out.println("File '"+fileName+"' was found!");
			try /*(FileOutputStream fos = new FileOutputStream(fileName);
	                OutputStreamWriter osw = new OutputStreamWriter(fos, 
	                        StandardCharsets.UTF_8);
	                CSVWriter csvWriter = new CSVWriter(osw))*/{
				FileWriter fileWriter = new FileWriter(fileName);
				CSVWriter csvWriter = new CSVWriter(fileWriter,
				        CSVWriter.DEFAULT_SEPARATOR,
				        CSVWriter.NO_QUOTE_CHARACTER,
				        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
				        CSVWriter.DEFAULT_LINE_END);
				
				csvWriter.writeAll(records);
	            fileWriter.flush();
				fileWriter.close();
	            //csvWriter.flush();
	            csvWriter.close();
	            records = null;
	            //System.out.println("File '"+fileName+"' insertion COMPLETED!");
	        }catch (Exception e) {
				// TODO: handle exception
	        	//e.printStackTrace();
			}
		} else {
			System.out.println("File '"+fileName+"' NOT found!");
		}
    	
    	
    	
    	
    	
    	/*String[] header2 = {"X","Y","cluster","silhouette"};
    	String[] data= {"23.2","34.34","c1","0.54"};
    	String[] data1= {"34.2","23.34","c2","0.74"};
    	String[] data2= {"12.2","76.34","c3","0.34"};
    	String[] data3= {"54.2","2.34","c4","0.84"};
    	
    	
    	
    	List<String[]> records = new ArrayList<>();
    	records.add(header2);
    	records.add(data);
    	records.add(data1);
    	records.add(data2);
    	records.add(data3);
    	
    	File sp = new File(fileName);
        boolean exists = sp.exists();
    	if (exists) {
			System.out.println("File '"+fileName+"' was found!");
			try (FileOutputStream fos = new FileOutputStream(fileName);
	                OutputStreamWriter osw = new OutputStreamWriter(fos, 
	                        StandardCharsets.UTF_8);
	                CSVWriter csvWriter = new CSVWriter(osw)){
				FileWriter fileWriter = new FileWriter(fileName);
				CSVWriter csvWriter = new CSVWriter(fileWriter,
				        CSVWriter.DEFAULT_SEPARATOR,
				        CSVWriter.NO_QUOTE_CHARACTER,
				        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
				        CSVWriter.DEFAULT_LINE_END);
				
	            csvWriter.writeNext(header2);
	            csvWriter.writeNext(data);
	            csvWriter.writeNext(data1);
	            csvWriter.writeNext(data2);
	            csvWriter.writeNext(data3);
				//csvWriter.writeAll(records);
	            fileWriter.flush();
				fileWriter.close();
	            //csvWriter.flush();
	            csvWriter.close();
	            System.out.println("File '"+fileName+"' insertion COMPLETED!");
	        }catch (Exception e) {
				// TODO: handle exception
	        	//e.printStackTrace();
			}
		} else {
			System.out.println("File '"+fileName+"' NOT found!");
		}*/
    	
    	
    	/*StringWriter writer = new StringWriter();
    	
    	//using custom delimiter and quote character
    	CSVWriter csvWriter = new CSVWriter(writer, ',', '\'');
    	
    	List<String[]> data;
    	
    	List<String[]> records = new ArrayList<String[]>();
    	
    	String[] header = {"id","name","age","country","silhouette"};
    	
    	// adding header record
    	records.add(header);
    	
    	data = records;
    	csvWriter.writeAll(data);
    	csvWriter.close();*/
    	
	}

    private void cleanInstances(Instances instances) {
        instances.setClassIndex(-1);
        instances.deleteAttributeAt(instances.numAttributes()-1);
    }

    private void writeDataSet(BufferedWriter writerBest, Instances test) {
        try {
            writerBest.write(test.toString());
            writerBest.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void classify(Classifier classifier, Instances test, Attribute cluster) {
        test.insertAttributeAt(cluster, test.numAttributes());
        test.setClassIndex(test.numAttributes()-1);
        for (int i = 0; i < test.numInstances(); i++) {
            Instance toBeTested = test.instance(i);
            double clazz = 0;
            try {
                clazz = classifier.classifyInstance(toBeTested);
            } catch (Exception e) {
                e.printStackTrace();
            }
            toBeTested.setValue(toBeTested.numAttributes()-1,cluster.value((int)clazz));
        }
       
    }

    private void buildClassifier(Classifier classifier, Instances instances) {
        try {
            //System.out.println(instances);
            classifier.buildClassifier(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //debugando
    private Attribute createAttribute(int n){
    	
    	while (n == 0 || n == 1) {
    		Random rand = new Random();
			n = 10;
		    n = rand.nextInt(n + 1);
		}
//    	if (n == 0) {
//			//n = 2;
//    		Random rand = new Random();
//			n = 10;
//		    n = rand.nextInt(n + 1);
//		}
        FastVector fastVector = new FastVector(n);
        for (int i = 0; i < n; i++)
            fastVector.addElement(new String("C"+(i<10?"0"+i:i)));
        Attribute cluster = new Attribute("cluster", fastVector);

        return cluster;        
    }
   
    //debugando
    private void trainDatabase(Individual best, Attribute cluster){
        Instances copyOfInstances = best.getInstances();
        List<Group> groups = best.getGroups();
        //System.out.println("Tamanho de best.getGroups(): "+best.getGroups());
        int tamCluster = cluster.numValues();
        //System.out.println("Print ´tamCluster´: "+tamCluster);
        copyOfInstances.insertAttributeAt(cluster, copyOfInstances.numAttributes());
        for (int i = 0; i < groups.size(); i++) {
            for ( Instance instance : groups.get(i).getInstances() ) {
                instance.setValue(instance.numAttributes()-1, cluster.value(i));
            }
        }
        copyOfInstances.setClassIndex(copyOfInstances.numAttributes()-1);
    }
   
    private BufferedWriter buildWriter(StringBuffer folder, StringBuffer name, String fileType){
        try {
            return new BufferedWriter(new FileWriter(new File(folder+"/"+name+fileType)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
   
    public ClusteringMethod simpleFactory(String name) throws Exception{
        return (ClusteringMethod) Class.forName(name).newInstance();
    }
   
    private Collection<Node> loadConfig(int slices) throws Exception{
        AutoClusteringVO config = VOLoad.load();
        SortedMap<Integer, Node> nodes = new TreeMap<Integer, Node>();
        for ( NodeVO nodeVO : config.getNodes() ) {
            Node node = new Node(simpleFactory(nodeVO.getClazz()), nodeVO.isOptk(), nodeVO.isOptOver());
            for ( ParameterVO p : nodeVO.getProperties() ) {
                ParameterOptions.putParameter(p, slices);
                node.getParameters().add(p.getType());
            }
            nodes.put(nodeVO.getNumber(), node);
        }
       
        for (EdgeVO edgeVO : config.getEdges())
            new Edge( nodes.get(edgeVO.getIn()),nodes.get(edgeVO.getOut()));
        return nodes.values();
    }
}