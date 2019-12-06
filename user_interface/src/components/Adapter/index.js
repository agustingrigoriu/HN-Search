import axios from "axios";

class SolrAPI {
  constructor() {
    this.url = "http://localhost:8983/solr/hncore/hnselect";
  }

  search = async stateQuery => {
    const serializedRequest = new MyRequestSerializer().serialize(stateQuery);
    try {
      const response = await axios.get(this.url, serializedRequest);
      console.log(response);
      const data = response.data;
      const serializedResponse = new MyResponseSerializer().serialize(data);
      console.log(serializedResponse);
      return serializedResponse;
    } catch (error) {
      console.error(error.message);
    }
  };
}

class MyRequestSerializer {
  serialize = stateQuery => {
    const 
      activePage = stateQuery.page,
      page = activePage > 0 ? activePage - 1 : 0,
      size = stateQuery.size,
      start = page * size;

    const payload = {
      params: {
        q: stateQuery.query,
        start: start,
        wt: "json"
      }
    };
    return payload;
  };
}

class MyResponseSerializer {
  serialize = payload => {
    return {
      hits: payload.response.docs,
      total: payload.response.numFound,
      start: payload.response.start,
      highlights: payload.highlighting,
      queryTime: payload.responseHeader.QTime
    };
  };
}

export default SolrAPI;
