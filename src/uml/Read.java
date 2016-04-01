package uml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.dom4j.Element;

public class Read
{
	ArrayList<WJLifeline> umlLifeLines=new ArrayList<WJLifeline>();
	ArrayList<MessageClass> umlMessages=new ArrayList<MessageClass>();
	ArrayList<ConnectorsClass> umlConnectors=new ArrayList<ConnectorsClass>();
	ArrayList<MessageComplete> umlMessageComplete=new ArrayList<MessageComplete>();
	ArrayList<WJFragment> umlFragment=new ArrayList<WJFragment>();
	ArrayList<WJFragment> umlFragmentInner=new ArrayList<WJFragment>();
	ArrayList<WJMessage> umlMsgFragment=new ArrayList<WJMessage>();
	ArrayList<WJDiagramsData> umlAllDiagramData = new ArrayList<WJDiagramsData>();
	
	HashMap<String , String> findAltsFather = new HashMap<String , String>();
	public boolean hasNoLifeline()
	{	
		if(umlLifeLines.isEmpty())                             	
			return true;
		else 
			return false;	
	}
	
	public  void load(Element root) throws Exception
	{
		ArrayList<Element> EAlifeLineList=new ArrayList();
		ArrayList<Element> EAconnectorList=new ArrayList();
		ArrayList<Element> EAfragmentList=new ArrayList();
		ArrayList<Element> EAmessagesList = new ArrayList();
		
		
		EAlifeLineList.addAll(root.element("Model").element("packagedElement").element("packagedElement").element("ownedBehavior").elements("lifeline"));
		EAconnectorList.addAll(root.element("Extension").element("connectors").elements("connector"));
		EAfragmentList.addAll(root.element("Model").element("packagedElement").element("packagedElement").element("ownedBehavior").elements("fragment"));
		EAmessagesList.addAll(root.element("Model").element("packagedElement").element("packagedElement").element("ownedBehavior").elements("message"));
		//��ȡlifeline��Ϣ
		for(Iterator<Element> lifeLineIterator=EAlifeLineList.iterator();lifeLineIterator.hasNext();)
		{
			Element elLifeLine=lifeLineIterator.next();
			WJLifeline lifeLine=new WJLifeline();
			lifeLine.setlifeLineId(elLifeLine.attribute("id").getValue());
			lifeLine.setlifeLineName(elLifeLine.attribute("name").getValue());
			umlLifeLines.add(lifeLine);
		}
		//��ȡmessage��Ϣ
		for(Iterator<Element> EAmessagesIterator=EAmessagesList.iterator();EAmessagesIterator.hasNext();)
		{
			Element elmessage=EAmessagesIterator.next();
			MessageClass message=new MessageClass();
			message.setSequenceMsgId(elmessage.attribute("id").getValue());
			message.setSequenceMsgName(elmessage.attribute("name").getValue());
			message.setSequenceMsgSendEvent(elmessage.attribute("sendEvent").getValue());
			message.setMessageSort(elmessage.attribute("messageSort").getValue());
			umlMessages.add(message);
		}
		//��ȡconnectors��Ϣ
		for(Iterator<Element> connectorIterator=EAconnectorList.iterator();connectorIterator.hasNext();)
		{
			Element elConnector=connectorIterator.next();
			ConnectorsClass connectorsMsg=new ConnectorsClass();
			connectorsMsg.setConnectorId(elConnector.attribute("idref").getValue());
			connectorsMsg.setSourceId(elConnector.element("source").attribute("idref").getValue());
			connectorsMsg.setTragetId(elConnector.element("target").attribute("idref").getValue());
			connectorsMsg.setName(elConnector.element("properties").attribute("name").getValue());

			if (elConnector.element("style").attribute("value")!=null) {
				String styleValue=elConnector.element("style").attribute("value").getValue();
				connectorsMsg.setStyleValue(styleValue);
			} 
			umlConnectors.add(connectorsMsg);
		}
		//����message��connectors����Ϣ ����connectors�е�source��targetID ���뵽������
		for(Iterator<MessageClass> umlMessagesIterator=umlMessages.iterator();umlMessagesIterator.hasNext();)
		{
			MessageClass messageI = umlMessagesIterator.next();
			MessageComplete messageComplete=new MessageComplete();
			messageComplete.name = messageI.getSequenceMsgName();
			messageComplete.connectorId = messageI.getSequenceMsgId();
			messageComplete.sendEvent = messageI.getSequenceMsgSendEvent();
			messageComplete.messageSort = messageI.getMessageSort();
			
			for(Iterator<ConnectorsClass> umlConnectorsIterator=umlConnectors.iterator();umlConnectorsIterator.hasNext();)
			{
				ConnectorsClass connectorsI=umlConnectorsIterator.next();
				if(connectorsI.getConnectorId().equals(messageI.getSequenceMsgId()))
				{
					messageComplete.sourceId = connectorsI.getSourceId();
					messageComplete.tragetId = connectorsI.getTragetId();
					messageComplete.styleValue = connectorsI.getStyleValue();
				}
			}
			umlMessageComplete.add(messageComplete);
		}
//***************************************	 ���Ƭ�ε�Ƕ�׶�ȡ			
		for(Iterator<Element> fragListIterator=EAfragmentList.iterator();fragListIterator.hasNext();)//��������fragment
		{
			Element fragment=fragListIterator.next();
			if(fragment.attribute("type").getValue().equals("uml:CombinedFragment"))
			{//�����Ƭ��
				Queue<Element> q = new LinkedList<Element>();
				Queue<String> q_AorP = new LinkedList<String>();
				Queue<String> q_BigId = new LinkedList<String>();
				q_BigId.add("null");
				q.add(fragment);
				while(!q.isEmpty())
				{
					Element parent = q.poll();
					/*if(parent.attribute("type").getValue().equals("uml:CombinedFragment"))//��ӡpop���Ƭ������loop
						System.out.println("pop:"+parent.attribute("interactionOperator").getValue());
					else																//��ӡalt����par
						System.out.println("pop:"+q_AorP.peek()+parent.attribute("id").getValue());*/
					
					ArrayList<Element> alfrags=new ArrayList<Element>();
					
					if(parent.attribute("type").getValue().equals("uml:CombinedFragment"))//���������Ƭ��
					{
						
						WJFragment fragInfo = new WJFragment();
						//visit(parent);
						fragInfo.setFragId(parent.attribute("id").getValue());					
						fragInfo.setFragType(parent.attribute("interactionOperator").getValue());//loop
					
						if(fragInfo.getFragType().equals("opt") || fragInfo.getFragType().equals("loop") || fragInfo.getFragType().equals("break"))//��alt par ֻ��1��operand
						{
							//System.out.println(parent.element("operand").element("guard").element("specification").attribute("body").getValue());
							fragInfo.setFragCondition(parent.element("operand").element("guard").element("specification").attribute("body").getValue());
							alfrags.addAll(parent.element("operand").elements("fragment"));
								//�ܶ�fragment ����Ǩ��(uml:OccurrenceSpecification������Ƕ�����Ƭ��(uml:CombinedFragment)																														
							Iterator<Element> alfragsIterator = alfrags.iterator();
							
							ArrayList<String> sID= new ArrayList<String>();
							ArrayList<String> tID= new ArrayList<String>();
							while(alfragsIterator.hasNext())//������һ��operand������fragment
							{
								Element child_fragsI = alfragsIterator.next();
								
								if(child_fragsI.attribute("type").getValue().equals("uml:OccurrenceSpecification"))
								{	
									sID.add(child_fragsI.attribute("id").getValue());
									child_fragsI = alfragsIterator.next();
									tID.add(child_fragsI.attribute("id").getValue());
									
								}
								else if(child_fragsI.attribute("type").getValue().equals("uml:CombinedFragment"))
								{
									q.add(child_fragsI);
									q_BigId.add(parent.attribute("id").getValue());
									//System.out.println("push:"+child_fragsI.attribute("interactionOperator").getValue());
									if(child_fragsI.attribute("interactionOperator").getValue().equals("alt")
											||child_fragsI.attribute("interactionOperator").getValue().equals("par"))
										findAltsFather.put(child_fragsI.attribute("id").getValue(), parent.attribute("id").getValue());
								}
							}		
							String[] s = new String[sID.size()];
							sID.toArray(s);
							String[] t = new String[tID.size()];
							tID.toArray(t);
							fragInfo.setSourceId(s);
							fragInfo.setTargetId(t);
							fragInfo.setBigId(q_BigId.poll());
							umlFragment.add(fragInfo);						
							
						}//��alt par ֻ��1��operand
						else if(fragInfo.getFragType().equals("alt") || fragInfo.getFragType().equals("par"))//alt ||par ���operand
						{
							ArrayList<Element> operandList=new ArrayList();
							operandList.addAll(parent.elements("operand"));//alt������С������
							q_BigId.poll();
							
							for(Iterator<Element> operandIterator=operandList.iterator();operandIterator.hasNext();)//��������operand
							{
								Element operandI = operandIterator.next();
								
								q.add(operandI);
								q_AorP.add(parent.attribute("interactionOperator").getValue());
								q_BigId.add(parent.attribute("id").getValue());
							}
							
													
							//System.out.println("��һ��"+parent.attribute("interactionOperator").getValue());
						}
						
					}
					else if(parent.attribute("type").getValue().equals("uml:InteractionOperand"))//�����Ƭ���еĲ�����
					{
						WJFragment fragInfo = new WJFragment();
						//visit(parent);
						
						fragInfo.setFragId(parent.attribute("id").getValue());					
						fragInfo.setFragType(q_AorP.poll());//���� alt����par
						fragInfo.setFragCondition(parent.element("guard").element("specification").attribute("body").getValue());
						
						alfrags.addAll(parent.elements("fragment"));
						///
						Iterator<Element> alfragsIterator = alfrags.iterator();
						
						ArrayList<String> sID= new ArrayList<String>();
						ArrayList<String> tID= new ArrayList<String>();
						while(alfragsIterator.hasNext())//������һ�������������fragment
						{
							Element child_fragsI = alfragsIterator.next();
							
							if(child_fragsI.attribute("type").getValue().equals("uml:OccurrenceSpecification"))
							{	
								sID.add(child_fragsI.attribute("id").getValue());
								child_fragsI = alfragsIterator.next();
								tID.add(child_fragsI.attribute("id").getValue());
								
 							}
							else if(child_fragsI.attribute("type").getValue().equals("uml:CombinedFragment"))
							{
								q.add(child_fragsI);
								q_BigId.add(parent.attribute("id").getValue());
								//System.out.println("push:"+child_fragsI.attribute("interactionOperator").getValue());
								if(child_fragsI.attribute("interactionOperator").getValue().equals("alt")
										||child_fragsI.attribute("interactionOperator").getValue().equals("par"))
									findAltsFather.put(child_fragsI.attribute("id").getValue(), parent.attribute("id").getValue());
							}
						}		
						String[] s = new String[sID.size()];
						sID.toArray(s);
						String[] t = new String[tID.size()];
						tID.toArray(t);
						fragInfo.setSourceId(s);
						fragInfo.setTargetId(t);
						fragInfo.setBigId(q_BigId.poll());
						umlFragment.add(fragInfo);		
					}
						
					
				}
								
			}
		}
		Iterator iterator=umlFragment.iterator();  //��fragmentƬ�εĽṹ���е���
		while(iterator.hasNext())
		{
			WJFragment I = (WJFragment)iterator.next();
			String bigid=I.getBigId();
			if(findAltsFather.containsKey(bigid))//alt������ĸ�����alt Ϊ���㷨��ʵ�֣�����ĳ����游
			{
				I.setBigId(findAltsFather.get(bigid));
				I.setComId(bigid);
			}
			//����������alt�Ĳ������Ҳ����游���򽻻�bigID��alt's ID����comID��null��
			if(!I.getBigId().equals("null")&&I.getComId().equals("null")&&(I.getFragType().equals("alt")||I.getFragType().equals("par")))
    		{
    			String temp = I.getBigId();
    			I.setBigId(I.comId);
    			I.setComId(temp);	    			
    		}
		}
//***************************************	 ���Ƭ�ε�Ƕ�׶�ȡ		
		//����messageList
		ArrayList <MessageComplete> messageList = new ArrayList <MessageComplete>();
		Iterator <MessageComplete> msgComplete = umlMessageComplete.iterator();
		Iterator <Element> messageIterator = EAmessagesList.iterator();
		while(messageIterator.hasNext()&&msgComplete.hasNext())
		{
			Element messageI = messageIterator.next();
			MessageComplete MC = msgComplete.next();
			
			ArrayList<Element> allargument = new ArrayList<Element>();
			allargument.addAll(messageI.elements("argument"));
			Iterator <Element> allargIterator = allargument.iterator();
			String T1 = null,T2 = null,Energe = null,R1 = null,R2 = null;
			while(allargIterator.hasNext())
			{
				Element allargI = allargIterator.next();
				if(allargI.attributeValue("name").equals("T1"))
					T1=allargI.element("defaultValue").attributeValue("value");
				if(allargI.attributeValue("name").equals("T2"))
					T2=allargI.element("defaultValue").attributeValue("value");
				if(allargI.attributeValue("name").equals("Energe"))
					Energe=allargI.element("defaultValue").attributeValue("value");
				if(allargI.attributeValue("name").equals("R1"))
					R1=allargI.element("defaultValue").attributeValue("value");
				if(allargI.attributeValue("name").equals("R2"))
					R2=allargI.element("defaultValue").attributeValue("value");
			}
			
			MessageComplete messageX = new MessageComplete();
			messageX.setName(messageI.attributeValue("name"));
			messageX.setConnectorId(messageI.attributeValue("id"));
			messageX.setSourceId(messageI.attributeValue("sendEvent"));
			messageX.setTragetId(messageI.attributeValue("receiveEvent"));
			messageX.setFromId(MC.getSourceId());
			messageX.setToId(MC.getTragetId());
			messageX.setStyleValue(MC.styleValue);
			messageX.setT1(T1);
			messageX.setT2(T2);
			messageX.setEnerge(Energe);
			messageX.setR1(R1);
			messageX.setR2(R2);
			
			messageList.add(messageX);
		}
		//�趨message����ֵ 0.�趨����ֵ 1.����5��ʱ��Լ�� 2.���ĸ�fragment��
		for(Iterator<MessageComplete> messageListIterator=messageList.iterator();messageListIterator.hasNext();)
		{
			/////////////////////////EAmessage�ı���
			MessageComplete EAmessage=messageListIterator.next();
			WJMessage message=new WJMessage();
			//1.
			message.setName(EAmessage.getName());					//name
			message.setConnectorId(EAmessage.getConnectorId());//messageID						
			message.setSendEvent(EAmessage.getSendEvent());		//event	
			message.setSourceId(EAmessage.getSourceId());		//sourceid
			message.setTragetId(EAmessage.getTragetId());		//targetid
			message.setFromId(EAmessage.getFromId());
			message.setToId(EAmessage.getToId());
			message.setT1(EAmessage.getT1());
			message.setT2(EAmessage.getT2());
			message.setEnerge(EAmessage.getEnerge());
			message.setR1(EAmessage.getR1());
			message.setR2(EAmessage.getR2());
			//2.
			setMessageTimeDurations(message, EAmessage.getStyleValue());
			for(Iterator<WJFragment> fragIterator=umlFragment.iterator();fragIterator.hasNext();)
			{
				WJFragment fragment=fragIterator.next();
				boolean finish = false;
				String[] s = fragment.getSourceId();//һ��fragment�ж��source
				String[] t = fragment.getTargetId();
				for(int i=0; i< fragment.getSourceId().length;i++)//��message���ĸ�fragment��
					if(message.getSourceId().equals(s[i]) && message.getTragetId().equals(t[i]))
					{	
						message.setInFragId(fragment.getFragId());
						message.setInFragName(fragment.getFragType());
						finish = true;
						break;
					}
				
				if(finish)
					break;
			}
			umlMsgFragment.add(message);//���յõ���message
			
		}
		
		//��Ա���� umlAllDiagramData ��������ͼ��list
		//�������ͼ�İ���id���
		ArrayList<Element> EADiagramsList = new ArrayList();//��Ŷ�ȡ�õ���element
				
		//1.ȡ�����е�diagram 
		EADiagramsList.addAll(root.element("Extension").element("diagrams").elements("diagram"));
		
		//2.����EADiagramIDsList
		for(Iterator<Element>  EADiagramsListIterator=EADiagramsList.iterator();EADiagramsListIterator.hasNext();)
		{
			//ȡ�õ�i��ͼ
			Element diagramI=EADiagramsListIterator.next();
			
			//�������ͼ����elements 
			ArrayList <Element> elements = new ArrayList <Element>();
			elements.addAll(diagramI.element("elements").elements("element"));
			
			//����elements ����ids
			ArrayList <String> ids = new ArrayList<String>();	
			for(Iterator<Element>  elementsIterator=elements.iterator();elementsIterator.hasNext();)
			{
				Element elementI = elementsIterator.next();
				ids.add(elementI.attributeValue("subject").substring(13));//ȡ��13λ֮���id����
			}
			
			//�������ͼ��name
			String name = diagramI.element("properties").attributeValue("name");
			
			//����DiagramsData����
			WJDiagramsData diagramData = new WJDiagramsData();
			diagramData.ids = ids;
			diagramData.name = name;
			
			//��DiagramsData���� ��ӵ���Ա����umlAllDiagramData��
			umlAllDiagramData.add(diagramData);
		}
		
		
	}
	
//DCBMX=0;DCBMGUID={65DF8856-63B5-423a-9BD1-952DEA23D616};SEQDC=10000;SEQDO=10002;SEQTC=10003;SEQTO=10004;DCBM=10001;
	//message�趨5��ʱ��Լ��
	private void setMessageTimeDurations(WJMessage message, String styleValue) {
		if (styleValue == null) return;
		String[] strArray = styleValue.split(";");
		for (int i = 0; i < strArray.length; i++) {
			String[] nameAndValue = strArray[i].split("=");
			if (nameAndValue[0].equals("SEQDC")) {
				message.setSEQDC(nameAndValue[1]);
			} else if (nameAndValue[0].equals("SEQDO")){
				message.setSEQDO(nameAndValue[1]);
			} else if (nameAndValue[0].equals("SEQTC")){
				message.setSEQTC(nameAndValue[1]);
			} else if (nameAndValue[0].equals("SEQTO")){
				message.setSEQTO(nameAndValue[1]);
			} else if (nameAndValue[0].equals("DCBM")){
				message.setDCBM(nameAndValue[1]);
			}
		}
		
	}

	public ArrayList<WJDiagramsData> getUmlAllDiagramData() {
		return umlAllDiagramData;
	}

	public HashMap<String , String>	getfindAltsFather()
	{
		return findAltsFather;
	}
	public ArrayList<WJLifeline> getLifeLines()
	{
		return umlLifeLines;
	}
	
	public ArrayList<WJFragment> getUmlFragmentMsg()
	{
		return umlFragment;
	}
	public ArrayList<WJMessage> getUmlMsgFragment()
	{
		return umlMsgFragment;
	}
}
	

