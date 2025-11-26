import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Eye } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { type Batch } from "@/lib/api";

const statusColors = {
  CREATED: "bg-secondary text-secondary-foreground",
  IN_TRANSIT: "bg-warning text-warning-foreground",
  DELIVERED: "bg-success text-success-foreground",
  SOLD: "bg-primary text-primary-foreground",
};

interface BatchListProps {
  batches: Batch[];
  onRefresh?: () => void;
}

const BatchList = ({ batches, onRefresh }: BatchListProps) => {
  const navigate = useNavigate();

  if (batches.length === 0) {
    return (
      <div className="text-center py-8 text-muted-foreground">
        No batches found. Create your first batch to get started.
      </div>
    );
  }

  return (
    <div className="rounded-md border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Batch Number</TableHead>
            <TableHead>Product</TableHead>
            <TableHead>Quantity</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Location</TableHead>
            <TableHead>Created</TableHead>
            <TableHead className="text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {batches.map((batch) => (
            <TableRow key={batch.id}>
              <TableCell className="font-mono text-sm">{batch.batchNumber}</TableCell>
              <TableCell>{batch.productName}</TableCell>
              <TableCell>{batch.quantity} units</TableCell>
              <TableCell>
                <Badge className={statusColors[batch.status as keyof typeof statusColors]}>
                  {batch.status.replace("_", " ")}
                </Badge>
              </TableCell>
              <TableCell>{batch.currentLocation}</TableCell>
              <TableCell>{new Date(batch.createdAt).toLocaleDateString()}</TableCell>
              <TableCell className="text-right">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => navigate(`/track/${batch.id}`)}
                >
                  <Eye className="w-4 h-4 mr-1" />
                  Track
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};

export default BatchList;
