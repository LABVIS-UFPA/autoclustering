package br.rede.autoclustering.savedata;

import java.io.File;

public final class SaveData {

	public void saveParameterPerAlgorithm(File analysisFile, File directory) {
		// TODO Auto-generated method stub
		System.out.println("Classe t� pegando");
		
		String buildingBlock = "CandidatesBySNN->SNNByConnectiveness\"";
		switch (buildingBlock) {
		case "CandidatesBySNN->SNNByConnectiveness\"":
			File snn = new File(directory+"/SNN.csv");
			
			
			break;

		default:
			System.out.println("M�todo terminado.");
			break;
		}
	}
}
