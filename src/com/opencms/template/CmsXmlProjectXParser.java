/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsXmlProjectXParser.java,v $
 * Date   : $Date: 2000/05/12 07:43:56 $
 * Version: $Revision: 1.1 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.template;

import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.apache.xml.serialize.*;


import com.opencms.core.*;

/**
 * Implementation of the OpenCms XML parser interface for
 * the ProjectX parser.
 * 
 * @author Alexander Kandzior
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/05/12 07:43:56 $
 */
public class CmsXmlProjectXParser implements I_CmsXmlParser, I_CmsLogChannels {
    
    /** Prevents the parser from printing multiple error messages.*/
    private static boolean c_xercesWarning = false;
    
    /**
     * Parses the given text with the Xerces parser.
     * @param in Reader with the input text.
     * @return Parsed text as DOM document.
     * @exception Exception
     */
    public Document parse(Reader in) throws Exception { 
        //return DOMFactory.createParser(in, null).parseDocument();
        //DOMParser parser = new DOMParser();

        Document doc=null;    
        
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            InputSource input = new InputSource(in);    
	        doc = docBuilder.parse(input);
        } catch(SAXException e) {
            if(A_OpenCms.isLogging() && ! c_xercesWarning) {
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsXmlXercesParser] Cannot set parser feature for apache xerces XML parser.");
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsXmlXercesParser] This is NOT critical, but you should better use xerces 1.0.3 or higher.");
                c_xercesWarning = true;
            }
        }

        return doc;
    }    

    /**
     * Creates an empty DOM XML document.
     * Workarround caus original method is corruped
     * 
     * @author Michaela Schleich
     * @param docNod first Node in empty  XML document
     * @return Empty document.
     */
    public Document createEmptyDocument(String docNod) throws Exception {
        String docXml = new String("<?xml version=\"1.0\" encoding=\"" + C_XML_ENCODING + "\"?>");
		docXml = docXml+"<"+docNod+">"+"</"+docNod+">";
		StringReader reader = new StringReader(docXml);

		return parse(reader);
		//return (Document)(new DocumentImpl(null));
    }    
    
    /**
     * Used to import a node from a foreign document.
     * @param doc Destination document that should import the node.
     * @param node Node to be imported.
     * @return New node that belongs to the document <code>doc</code>
     */
    public Node importNode(Document doc, Node node) { 
         return importNode(doc,node, true); 
    }
    
    
    /**
     * Imports a node form a forigan node. This method is based on the Xerces implementation
     * of the importNode method.
     * @param doc Destination document that should import the node.
     * @param source Node to be imported.
     * @param deep Recursive flag.
     * @return New node that belongs to the document <code>doc</code>
     */
    private Node importNode(Document doc,Node source, boolean deep) {
        Node newnode=null;
        
        int type = source.getNodeType();
    	switch (type) {
    		
        case doc.ELEMENT_NODE: {
            	Element newelement;
                newelement = doc.createElement(source.getNodeName());
        	    NamedNodeMap srcattr = source.getAttributes();
		        if (srcattr != null) {
                    for(int i = 0; i < srcattr.getLength(); i++) {
                        Attr attr = (Attr) srcattr.item(i);
                        if (attr.getSpecified()) { // not a default attribute
                            Attr nattr = (Attr) importNode(doc, attr, true);
                            newelement.setAttributeNode(nattr);
                         }
                    }
                }
		        newnode = newelement;
		        break;
            }

        case doc.ATTRIBUTE_NODE: {
  		         newnode = doc.createAttribute(source.getNodeName());
                 newnode.setNodeValue(source.getNodeValue());
                 deep = true;
		        // Kids carry value
		        break;
            }

	    case doc.TEXT_NODE: {
		        newnode = doc.createTextNode(source.getNodeValue());
                break;
            }

	    case doc.CDATA_SECTION_NODE: {
		        newnode = doc.createCDATASection(source.getNodeValue());
		        break;
            }

    	case doc.ENTITY_REFERENCE_NODE: {
		        break;
            }

                                    
                                        
    	case doc.ENTITY_NODE: {
        		break;
            }

    	case doc.PROCESSING_INSTRUCTION_NODE: {
		        newnode = doc.createProcessingInstruction(source.getNodeName(),
						      source.getNodeValue());
		        break;
            }

    	case doc.COMMENT_NODE: {
		        newnode = doc.createComment(source.getNodeValue());
		        break;
            }

   	    case doc.DOCUMENT_TYPE_NODE: {
        		break;
            }

    	case doc.DOCUMENT_FRAGMENT_NODE: {
		        newnode = doc.createDocumentFragment();
	        	// No name, kids carry value
		        break;
            }

        case doc.NOTATION_NODE: {
    		break;
            }

    	case doc.DOCUMENT_NODE : // Document can't be child of Document
    	    default: {	
            }
        }

    	// If deep, replicate and attach the kids.
    	if (deep) {
	        for (Node srckid = source.getFirstChild();
                 srckid != null;
                 srckid = srckid.getNextSibling()) {
		        newnode.appendChild(importNode(doc,srckid, true));
	    }
        }
        if (newnode.getNodeType() == Node.ENTITY_REFERENCE_NODE
            || newnode.getNodeType() == Node.ENTITY_NODE) {
            
        //  ((NodeImpl)newnode).setReadOnly(true, true);
        }
            
    	return newnode;
    }
    

    /**
     * Calls a XML printer for converting a XML DOM document
     * to a String.
     * @param doc Document to be printed.
     * @param out Writer to print to.
     */
    public void getXmlText(Document doc, Writer out) {
        OutputFormat outf = new OutputFormat(doc, C_XML_ENCODING, true);
        outf.setLineWidth(C_XML_LINE_WIDTH);            
        outf.setPreserveSpace(false);
        
        XMLSerializer serializer = new XMLSerializer(out, outf);
        try {
            serializer.serialize(doc);
        } catch(Exception e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlXerxesParser] " + e);
            }
        }
    }

    /**
     * Calls a XML printer for converting a XML DOM document
     * to a String.
     * @param doc Document to be printed.
     * @param out OutputStream to print to.
     */
    public void getXmlText(Document doc, OutputStream out) {
        OutputStreamWriter osw = new OutputStreamWriter(out);
        getXmlText(doc, osw);
    }
    
    /**
     * Gets a description of the parser.
     * @return Parser description.
     */
    public String toString() {
        return "Sun ProjectX XML Parser";        
    }
}
