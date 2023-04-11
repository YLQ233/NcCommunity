package com.nc.nccommunity.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SensitiveFilter {
	private static final String REPLACEMENT = "***";
	private TrieNode root = new TrieNode();
	
	
	//Define TrieTree
	private class TrieNode {
		private boolean isEnd = false;
		private Map<Character,TrieNode> childNode = new HashMap<Character,TrieNode>();
		
		public boolean isEnd() {
			return this.isEnd;
		}
		
		public void setEnd(boolean isEnd) {
			this.isEnd = isEnd;
		}
		
		public TrieNode getChildNode(char c) {
			return childNode.get(c);
		}
		
		public void addChildNode(char c, TrieNode child) {
			childNode.put(c,child);
		}
	}
	
	
//Init TrieTree
	@PostConstruct // 容器实例化该Bean并调用constructor后,自动调用该方法,一般用于init方法
	private void initTrie(){
		// 读取敏感词文件
		try(// 此位置可自动关闭stream流 == finally中关闭流
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		){
			String keyWord;
			while((keyWord = reader.readLine()) != null){
				addKeyWord(keyWord);
			}
		}catch (IOException e){
			log.error("加载敏感词文件失败: " + e.getMessage());
		}
	}
	//addKeyWord to TrieTree
	private void addKeyWord(String keyWord){
		TrieNode tmp = root;
		for (int i=0; i<keyWord.length(); i++){
			char c = keyWord.charAt(i);
			TrieNode node = tmp.getChildNode(c);
			if(node == null){
				node = new TrieNode();
				tmp.addChildNode(c,node);
			}
			tmp = node;
			if(i == keyWord.length()-1)
				tmp.setEnd(true);
		}
	}
	
	
//filter KeyWords
	//return text after filter
	public String filter(String text){
		if(StringUtils.isBlank(text))
			return null;
		
		TrieNode pointer = root;
		int begin=0, end=0;
		StringBuilder sb = new StringBuilder();
		
		while(end < text.length()){
			char c = text.charAt(end);
			
			//跳过特殊符号
			if(!CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF)){ //是符号
				if(pointer == root){//符号不在敏感词中间，正常添加，指针同步后移
					sb.append(c);
					end = ++begin;
				}else{//符号在敏感词中,算作敏感词被替换，begin不动，end后移
					end++;
				}
				continue;
			}
			
			//c在Trie中
			if(pointer.childNode.containsKey(c)){
				pointer = pointer.getChildNode(c);
				//end是敏感词结束
				if(pointer.isEnd()){
					sb.append(REPLACEMENT);
					begin = ++end;
					pointer = root;
				}else{ //end不是敏感词结束
					end++;
				}
			}else{ //c不在Trie中
				sb.append(text.charAt(begin));
				end = ++begin;
				pointer = root;
			}
		}
		// 计入漏掉的最后一批字符（end到达结尾，begin未到）
		sb.append(text.substring(begin));
		
		return sb.toString();
	}
	
	
}
