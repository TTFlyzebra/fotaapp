package com.flyzebra.flydown.file;
/** 
* 功能说明：
* @author 作者：FlyZebra 
* @version 创建时间：2017年3月1日 下午1:47:37  
*/
public class FileBlock {
	//开始位置
	private long staPos = 0;
	//结速位置
	private long endPos = 0;	
	private int state = 0;//下载状态 0xff下载完成//下载状态0xfe 不支持断点续传
	private int tag = 0;
	private int order = 0;
	public long getStaPos() {
		return staPos;
	}

	public void setStaPos(long staPos) {
		this.staPos = staPos;
	}

	public long getEndPos() {
		return endPos;
	}

	public void setEndPos(long endPos) {
		this.endPos = endPos;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public static FileBlock create(String str){
		if(str==null||str.length()==0){
			return null;
		}
		try{
			long staPos = Long.parseLong(str.substring(0, 16), 16);
			long endPos = Long.parseLong(str.substring(16, 32), 16);
			int state = (int) Long.parseLong(str.substring(32, 34), 16);
			int tag = (int) Long.parseLong(str.substring(34, 41), 16);
			int order = (int) Long.parseLong(str.substring(41, 48), 16);
			FileBlock fileBlock = new FileBlock();
			fileBlock.setStaPos(staPos);
			fileBlock.setEndPos(endPos);
			fileBlock.setState(state);
			fileBlock.setTag(tag);
			fileBlock.setOrder(order);
			return fileBlock;
		}catch(Exception e){
			return null;
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("%016x", staPos))
		.append(String.format("%016x", endPos))
		.append(String.format("%02x", state))
		.append(String.format("%07x", tag))
		.append(String.format("%07x", order));
		return sb.toString();
	}
}
