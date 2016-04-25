/**
 * 
 */
package com.sdxz.svn;



class FilePath{
	private String base;
	private String context;
	private String name;
	private String extension;
	
	public final static String separator = "/";
	
//	public FilePath(String path){
//		if(path!=null){
//			if(path.endsWith(File.pathSeparator)){
//				context = path;
//			}else{
//				context = path.substring(0, path.lastIndexOf(File.pathSeparator));
//				
//			}
//		}
//	}
	public FilePath(){
	}
	
	public FilePath(FilePath filePath){
		setBase(filePath.getBase());
		setContext(filePath.getContext());
		setName(filePath.getName());
		setExtension(filePath.getExtension());
	}
	
	public void setBase(String base) {
		this.base = base;
	}
	public String getBase() {
		return base;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public String getContext() {
		return context;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public String getExtension() {
		return extension;
	}
	
	public void setFileName(String fileName){
		String[] names = fileName.split("[.]");
		
		if(names.length>1){
			setName(names[0]);
			for (int i = 1; i < names.length-1; i++) {
				setName(getName()+"."+names[i]);
			}
			setExtension(names[names.length-1]);
		}else{
			setName(fileName);
		}
	}
	
	public String getFileName(){
		if(getExtension()!=null&&getName()!=null){
			return getName()+"."+getExtension();
		}else if(getName()!=null){
			return getName();
		}else
			return null;
	}
	
	public String getContextPath(){
		return getContext()+separator+getName()+"."+getExtension();
	}
	
	public String toString(){
		return this.getBase()+this.getContext()+separator+this.getName()+"."+this.getExtension();
	}
}