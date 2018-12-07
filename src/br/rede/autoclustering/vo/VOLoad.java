package br.rede.autoclustering.vo;

import java.io.File;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.load.Persister;

public class VOLoad {

	public static AutoClusteringVO load() throws Exception{
		File configFile = new File("resources/config.xml");
		Serializer serializer = new Persister();
		AutoClusteringVO loaded = serializer.read(AutoClusteringVO.class, configFile);
		return loaded;
	}
}
