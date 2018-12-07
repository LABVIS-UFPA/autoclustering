package br.rede.autoclustering.util;

public enum DistanceType {

	EUCLIDEAN(0),
	MANHATTAN(1), 
	CHEBYCHEV(2), 
//	MINKOWSKI(3), 
	BRAYCURTIS(3);
//	CORRELATION_COEFFICIENT(5); 
//	CANBERRA(5),
//	ANGULAR_SEPARATION(6), 
	
	private int type;
	
	DistanceType( int type ) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
	public static DistanceType getDistanceType(int type){
		for ( DistanceType value : DistanceType.values() )
			if ( value.getType() == type )
				return value;
		return EUCLIDEAN;
	}
}
