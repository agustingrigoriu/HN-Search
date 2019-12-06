import React from "react";
import {
  Container,
  Form,
  Segment,
  Header,
  Item,
  Icon,
  Pagination
} from "semantic-ui-react";
import SolrAPI from "./components/Adapter";
import "semantic-ui-css/semantic.min.css";

const BASE_URL = "https://news.ycombinator.com/item?id=",
  PAGE_SIZE = 10;

export default class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      query: "",
      data: [],
      activePage: 1,
      solrAdapter: new SolrAPI()
    };

    this.querySearch = this.querySearch.bind(this);
    this.handleInputChange = this.handleInputChange.bind(this);
    this.displayResults = this.displayResults.bind(this);
  }

  async querySearch() {
    try {
      const data = await this.state.solrAdapter.search({
        query: this.state.query,
        page: 1,
        size: PAGE_SIZE
      });
      this.setState({ data: data, activePage: 1 });
    } catch (e) {
      console.error(e.message);
    }
  }

  handleInputChange(e) {
    this.setState({ query: e.target.value });
  }

  handlePaginationChange = async (e, { activePage }) => {
    try {
      const data = await this.state.solrAdapter.search({
        query: this.state.query,
        page: activePage,
        size: PAGE_SIZE
      });
      this.setState({ data, activePage });
    } catch (e) {
      console.error(e.message);
    }
  };

  getSnippet(highlight) {
    let snippet = "";
    for (var key in highlight) {
      let field = highlight[key] || [];
      snippet += field ? field : "";
    }

    return snippet;
  }

  displayResults() {
    const me = this,
      state = me.state || {},
      data = state.data || {},
      highlights = data.highlights || {},
      hits = data.hits || [];

    let results = hits.map(_result => {
      const result = _result || {},
        resultId = result.id || 0,
        resultURL = result.url || "",
        hitURL = `${BASE_URL}${resultId}` || "#",
        resultPP = result.popularity_points || "N/A",
        resultTitle = result.title || "N/A",
        resultAuthor = result.author || "N/A",
        resultCommentsNumber = result.comments_number || "N/A",
        highlight = highlights[resultId] || {},
        resultSnippet =
          this.getSnippet(highlight) || "No description available.",
        resultDate = new Date(result.timestamp).toLocaleDateString(),
        delimiter = <span>&nbsp;&nbsp;&nbsp;&nbsp;</span>;

      return (
        <Item style={{ marginBottom: "27px" }}>
          <Item.Content>
            <Item.Header
              style={{ color: "#0000EE", textDecoration: "underline" }}
              as="a"
              href={hitURL}
            >
              {resultTitle}
            </Item.Header>
            <Item.Meta>
              {resultURL && (
                <div>
                  <Icon name="linkify" />
                  <a href={resultURL}>{resultURL}</a>
                </div>
              )}
            </Item.Meta>
            <Item.Description>
              <div
                style={{ color: "#545454" }}
                dangerouslySetInnerHTML={{ __html: resultSnippet }}
              />
            </Item.Description>
            <Item.Extra>
              <Icon name="calendar outline" /> {resultDate}
              {delimiter}
              <Icon name="like" /> {resultPP}
              {delimiter}
              <Icon name="comments outline" /> {resultCommentsNumber}
              {delimiter}
              <Icon name="user" /> {resultAuthor}
            </Item.Extra>
          </Item.Content>
        </Item>
      );
    });

    return hits.length > 0 ? (
      results
    ) : (
      <Segment placeholder>
        <Header icon>
          <Icon name="frown" color="orange" />
          Nothing to show at the moment!
        </Header>
      </Segment>
    );
  }

  render() {
    const me = this,
      state = me.state || {},
      data = state.data || {},
      total = data.total || 0,
      totalPages = Math.ceil(total / PAGE_SIZE),
      hits = data.hits,
      queryTime = data.queryTime || 0,
      queryTimeSeconds = queryTime / 1000,
      message = `${total.toLocaleString()} results found (${queryTimeSeconds} seconds)`;

    return (
      <Container style={{ marginTop: "3em" }}>
        <Segment.Group>
          <Segment textAlign="left" padded>
            <Header as="h1" size="huge" style={{ color: "#0066ff" }}>
              <Icon
                style={{ color: "#ff6600" }}
                name="hacker news square"
              ></Icon>{" "}
              Search Engine
            </Header>
            <Form>
              <Form.Input
                fluid
                icon={
                  <Icon
                    name="search"
                    color="blue"
                    inverted
                    circular
                    link
                    onClick={() => this.querySearch()}
                  />
                }
                value={this.state.query}
                onChange={e => this.handleInputChange(e)}
                onKeyPress={event => {
                  if (event.key === "Enter") {
                    this.querySearch();
                  }
                }}
              />
            </Form>
          </Segment>

          <Segment>
            {hits && hits.length >= 0 ? (
              <div>
                <Header size="tiny">{message}</Header>
                <Item.Group>{this.displayResults()}</Item.Group>
                <Segment textAlign="center" vertical>
                  <Pagination
                    activePage={this.state.activePage}
                    onPageChange={this.handlePaginationChange}
                    totalPages={totalPages}
                  />
                </Segment>
              </div>
            ) : (
              <Segment placeholder>
                <Header icon>
                  <Icon name="search" color="orange" />
                  Welcome! Just type what you are looking for in the above text
                  box and press the search button or Enter.
                </Header>
              </Segment>
            )}
          </Segment>
        </Segment.Group>
      </Container>
    );
  }
}
